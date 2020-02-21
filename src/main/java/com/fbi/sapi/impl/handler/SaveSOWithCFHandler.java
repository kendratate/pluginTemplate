package com.fbi.sapi.impl.handler;


import com.evnt.eve.modules.logic.extra.LogicCustomField;
import com.evnt.eve.modules.logic.extra.LogicMemo;
import com.evnt.util.FbiMessage;
import com.fbi.fbo.impl.message.request.SaveSOWithCFRequestImpl;
import com.fbi.fbo.impl.message.response.MasterResponseImpl;
import com.fbi.fbo.impl.message.response.SaveSOWithCFResponseImpl;
import com.fbi.fbo.message.Response;
import com.fbi.fbo.message.request.SaveSOWithCFRequest;
import com.fbi.fbo.message.response.SaveSOWithCFResponse;
import com.fbi.fbo.orders.SalesOrder;
import com.fbi.util.FbiException;
import com.fbi.util.UtilXML;
import com.fbi.util.exception.ExceptionMainFree;
import com.fbi.util.logging.FBLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("SaveSOWithCFRq")
public class SaveSOWithCFHandler extends Handler{

    @Autowired
    private LogicCustomField logicCustomField;
    @Autowired
    private LogicMemo logicMemo;

    @Override
    public void execute(String request, int userId, Response response) {

        MasterResponseImpl masterResponse = (MasterResponseImpl)response;

        final SaveSOWithCFResponse saveResponse = new SaveSOWithCFResponseImpl();

        masterResponse.getResponseList().add(saveResponse);

        try {
            final SaveSOWithCFRequest saveRequest = (SaveSOWithCFRequest)UtilXML.getObject(request, SaveSOWithCFRequestImpl.class);
            SalesOrder so = saveRequest.getSO();
            so.setIssueFlag(saveRequest.getIssueFlag());
            SalesOrder newSo = this.getOrderManager().saveSalesOrder(so, true, saveRequest.isIgnoreItems(), false);
            //now we need to save the custom fields and the memos again and this time commit the transaction

            if (so.hasCustomFields()) {

                logicCustomField.saveCustomFields(so);
            }

            if (so.hasMemos()) {
                logicMemo.saveMemos(so);
            }

            //Shouldnt need in 2019.10, the logicCustomFields saves and flushes the records.
            //TODO: needs tested on regular API call to see if it saves the Custom Fields
            //this.getTransactionRepository().commit();


            saveResponse.setSO(newSo);

        } catch (FbiException var8) {
            FBLogger.error(var8.getMessage(), var8);
            saveResponse.setStatusCode(var8.getStatusCode());
            saveResponse.setStatusMessage(var8.getMessage());
        } catch (ExceptionMainFree var9) {
            FBLogger.error(var9.getMsgErr(), var9);
            saveResponse.setStatusCode(var9.getCode());
            saveResponse.setStatusMessage(var9.getMsgErr());
        }

    }
}