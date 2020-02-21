package com.fbi.sapi.impl.handler;

import com.fbi.entity.plugin.PluginPropertyRepository;
import com.fbi.fbdata.plugins.PluginPropertyFpo;
import com.fbi.fbo.impl.message.response.GetServerPrinterListResponseImpl;
import com.fbi.fbo.impl.message.response.MasterResponseImpl;
import com.fbi.fbo.message.Response;
import com.fbi.fbo.message.response.GetServerPrinterListResponse;
import com.fbi.util.FbiException;
import com.fbi.util.exception.ExceptionMainFree;
import com.fbi.util.logging.FBLogger;
import com.printnode.api.APIClient;
import com.printnode.api.Auth;
import com.printnode.api.Printer;
import com.unigrative.plugins.apiExtension.ApiExtensionsPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("GetServerPrinterListRq")
public class GetServerPrinterListHandler extends Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger((Class) GetServerPrinterListHandler.class);

    @Override
    public void execute(String s, int i, Response response) {

        MasterResponseImpl masterResponse = (MasterResponseImpl) response;

        final GetServerPrinterListResponse printerListResponse = new GetServerPrinterListResponseImpl();

        masterResponse.getResponseList().add(printerListResponse);

        //get API key

        PluginPropertyFpo propertyFpo = this.getPluginPropertyRepository().findByPluginAndKey(ApiExtensionsPlugin.MODULE_NAME, "PrintNodeApiKey");
        String apiKey = propertyFpo.getInfo();

        Auth auth = new Auth();
        auth.setApiKey(apiKey);

        APIClient client = new APIClient(auth);

        try {
            List<Printer> printers = Arrays.asList(client.getPrinters(""));

            if (printers.size() == 0){
               throw new FbiException("No printers found under account: " + client.getWhoami().getEmail());
            }

            printerListResponse.setPrinters((ArrayList)printers);

         }
         catch (FbiException var8) {
             FBLogger.error(var8.getMessage(), var8);
             response.setStatusCode(var8.getStatusCode());
             response.setStatusMessage(var8.getMessage());

         }
         catch (IOException ex){
            printerListResponse.setStatusCode(9000);
            printerListResponse.setStatusMessage(ex.getMessage());
         }


//
//
//
//
//
//        ArrayList<String> printerList = new ArrayList();
//
//        try {
//            refreshSystemPrinterList();
//
//            //locate the printer on the machine
//            final PrintService[] lookupPrintServices = PrintServiceLookup.lookupPrintServices(null, null);
//            for (final PrintService service : lookupPrintServices) {
//                printerList.add(service.getName());
//            }
//
//            printerListResponse.setPrinters(printerList);
//
//
//
//        } catch (ExceptionMainFree e2) {
//            FBLogger.error(e2.getMessage(), e2);
//            printerListResponse.setStatusCode(e2.getCode());
//            printerListResponse.setStatusMessage(e2.getMsgErr());
//        }

    }

    /**
     * Printer list does not necessarily refresh if you change the list of
     * printers within the O/S; you can run this to refresh if necessary.
     */
    public static void refreshSystemPrinterList() {

        Class[] classes = PrintServiceLookup.class.getDeclaredClasses();

        for (int i = 0; i < classes.length; i++) {

            if ("javax.print.PrintServiceLookup$Services".equals(classes[i].getName())) {

                sun.awt.AppContext.getAppContext().remove(classes[i]);
                break;
            }
        }
    }
}
