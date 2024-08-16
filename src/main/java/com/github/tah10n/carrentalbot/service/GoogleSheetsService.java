package com.github.tah10n.carrentalbot.service;

import com.github.tah10n.carrentalbot.config.GoogleSheetsConfig;
import com.github.tah10n.carrentalbot.db.entity.BookingHistory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class GoogleSheetsService {
    private final Sheets service;
    private final GoogleSheetsConfig config;
    private final String SPREADSHEET_ID;
    private final CarService carService;
    final String range = "A2:G";

    public GoogleSheetsService(Sheets service, GoogleSheetsConfig config, CarService carService) {
        this.service = service;
        this.config = config;
        SPREADSHEET_ID = config.getSpreadsheetId();
        this.carService = carService;
    }

    public ValueRange getValues() throws IOException {

        return service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) {
//            log.error("No data found from google sheets.");
//        } else {
//            for (List row : values) {
////                System.out.printf("%s\n", row.get(0));
//            }
//        }
    }

    public AppendValuesResponse appendValues(BookingHistory bookingHistory) throws IOException {

        ValueRange appendBody = new ValueRange()
                .setValues(Arrays.asList(
                        Arrays.asList(carService.getBookingHistoryValues(bookingHistory.getId()))));
        AppendValuesResponse appendResult = service.spreadsheets().values()
                .append(SPREADSHEET_ID, "A1", appendBody)
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
        return appendResult;
    }

    public void deleteValues(String bookingHistoryId) throws IOException {
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            log.error("No data found from google sheets.");
        } else {
            for (int i = 0; i < values.size(); i++) {
                List<Object> row = values.get(i);
                String id = (String) row.get(0);

                if (id.equals(bookingHistoryId)) {
                        // найдена строка с словом
                        int startIndex = i + 1; // индекс строки (начиная с 1)
                        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
                        batchUpdateRequest.setRequests(Arrays.asList(
                                new Request()
                                        .setDeleteDimension(new DeleteDimensionRequest()
                                                .setRange(new DimensionRange()
                                                        .setDimension("ROWS")
                                                        .setStartIndex(startIndex)
                                                        .setEndIndex(startIndex + 1)
                                                        .setSheetId(0)))
                        ));
                        service.spreadsheets().batchUpdate(SPREADSHEET_ID, batchUpdateRequest).execute();
                        log.warn("Deleted row with id: " + bookingHistoryId);
                        return;
                    }

            }
        }
    }
}
