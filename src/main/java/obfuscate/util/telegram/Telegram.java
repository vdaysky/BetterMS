package obfuscate.util.telegram;

import obfuscate.MsdmPlugin;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.IOException;

public class Telegram {

    public static void sendMessage(String message) {
        try {
            MsdmPlugin.getTgBot().execute(
                    new SendMessage(MsdmPlugin.Config.getChatId(), message),
                    new Callback<SendMessage, SendResponse>() {
                        @Override
                        public void onResponse(SendMessage sendMessage, SendResponse sendResponse) {

                        }

                        @Override
                        public void onFailure(SendMessage sendMessage, IOException e) {

                        }
                    }
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
