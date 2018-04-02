package model.interfaces;

import messaging.requestreply.RequestReply;

public interface ClientInterface {
    void next(RequestReply requestReply);
}
