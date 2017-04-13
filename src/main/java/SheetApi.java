import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.compute.ComputeCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.http.protocol.HTTP;
import org.mortbay.util.ajax.JSON;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Johann Bernez
 */
public class SheetApi {

    private static HttpTransport HTTP_TRANSPORT;

    private static final String APPLICATION_NAME = "sheet-api";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final Set<String> SCOPES = Collections.singleton(SheetsScopes.SPREADSHEETS);

    private static final Logger LOG = Logger.getLogger(SheetApi.class.getSimpleName());

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException {
        // Build a new authorized API client service.
        Sheets service = getSheetsService();

        // ID of the spread sheet
        String spreadsheetId = "1dLfAagLzYVvk-zWrrxeC2kSQzekivPsVpNzEbX7LHoY";

        String range = "B2";
        Sheets.Spreadsheets.Values.Get request = service.spreadsheets().values().get(spreadsheetId, range);

        // https://developers.google.com/sheets/api/reference/rest/v4/ValueRenderOption
        request.setValueRenderOption("FORMATTED_VALUE");

        ValueRange response = request.execute();
        List<List<Object>> values = Optional.of(response.getValues()).orElse(Collections.emptyList());
        if (values.isEmpty()) {
            LOG.info("No value at the given cell");
        } else {
            LOG.info(String.format("The value is %s", values.iterator().next().iterator().next()));
        }
    }

    private static Sheets getSheetsService() throws IOException {
        GoogleCredential credential = GoogleCredential
                .fromStream(SheetApi.class.getResourceAsStream("sheet-api-secret.json"), HTTP_TRANSPORT, JSON_FACTORY)
                .createScoped(SCOPES);

        credential.refreshToken();

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
