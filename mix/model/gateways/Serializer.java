package model.gateways;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import messaging.requestreply.RequestReply;
import model.loan.LoanReply;

public class Serializer<REQUEST, REPLY> {
    private Gson gson; //gebruikte eerst genson maar die heeft slechte ondersteuning voor generics zoals RequestReply, terwijl gson dit met gemak ondersteund

    private final Class<REQUEST> requestClass;
    private final Class<REPLY> replyClass;

    public Serializer(Class<REQUEST> requestClass, Class<REPLY> replyClass){
        this.requestClass = requestClass;
        this.replyClass = replyClass;
        gson = new GsonBuilder().create();
    }

    public String requestToString(REQUEST request){
        return gson.toJson(request);
    }

    public REQUEST requestFromString(String str){
        return gson.fromJson(str, requestClass);
    }

    public RequestReply requestReplyFromString(String str){
        return gson.fromJson(str, TypeToken.getParameterized(RequestReply.class, requestClass, replyClass).getType());
    }

    public String requestReplyToString(RequestReply rr){
        return gson.toJson(rr);
    }

    public String replyToString(REPLY reply){
        return gson.toJson(reply);
    }

    public REPLY replyFromString(String str){
        return gson.fromJson(str, replyClass);
    }
}
