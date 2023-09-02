package obfuscate.util.hotbar;

import obfuscate.game.player.StrikePlayer;
import obfuscate.util.time.Task;

import java.util.ArrayList;
import java.util.function.Function;

public class SimpleHotbarMessenger
{
    private Function<Void, String> message;
    private ImportantMessage activeImportantMessage = null;

    private final ArrayList<ImportantMessage> importantMessages = new ArrayList<>();
    private final StrikePlayer holder;

    public SimpleHotbarMessenger(StrikePlayer holder)
    {
        this.holder = holder;
    }

    /** this class is not self sufficient. It depends on updates from game class or whatever class i want to use. */
    public void update()
    {
        String msg_text = getMessageText();
        if (msg_text == null)
            return;

        if (this.holder.isOnline()) {
            this.holder.sendHotBar(msg_text);
        }
    }

    private String getMessageText()
    {
        if (activeImportantMessage == null) {
            if (message == null)
                return null;

            return message.apply(null);
        }
        if (activeImportantMessage.expired()) {
            activeImportantMessage = getImportantMessage();
            return getMessageText();
        }
        String textFromRunnable = activeImportantMessage.getMessage();
        if (textFromRunnable == null) {
            // prioritized message ended sooner then expected
            activeImportantMessage = getImportantMessage();
            return getMessageText();
        }
        return textFromRunnable;
    }

    public void setMessage(Function<Void, String> message)
    {
        this.message = message;
        update();
    }

    public void setMessage(String message)
    {
        this.message = aVoid -> message;
        update();
    }

    public void importantMessage(Function<Void, String> message, int ms_duration, Integer t_updateRate)
    {
        updateImportantMessage(new ImportantMessage(message, ms_duration, t_updateRate));
    }

    public void importantMessage(Function<Void, String> message, int ms_duration)
    {

        importantMessage(message, ms_duration, null);
    }

    public void importantMessage(String message, int ms_duration)
    {
        importantMessage(message, ms_duration, null);
    }

    public void importantMessage(String message, int ms_duration, Integer t_updateRate)
    {
        importantMessage(aVoid -> message, ms_duration, t_updateRate);
    }

    private void updateImportantMessage(ImportantMessage message)
    {
        addImportantMessageToQueue(activeImportantMessage);
        //updateRate(message.updateRate);
        activeImportantMessage = message;
        // schedule update that will see that message expired
        new Task(this::update, message.delay_ticks+1).run();
        update();
    }

//    private void updateRate(int t_rate)
//    {
//        if (updateRate != t_rate)
//        {
//            updateRate = t_rate;
//            updateTask.cancel();
//            updateTask.setRepeat(t_rate);
//            updateTask.run();
//        }
//    }

    public void clearMessage()
    {
        message = null;
        importantMessages.clear();
        activeImportantMessage = null;
        //updateTask.cancel();
    }

    private void addImportantMessageToQueue(ImportantMessage m)
    {
        if (m == null) return;
        importantMessages.add(m);
    }

    private ImportantMessage getImportantMessage()
    {
        if (importantMessages.isEmpty()){
            //updateRate(DEFAULT_UPDATE_RATE);
            return null;
        }
        int last = importantMessages.size()-1;

        ImportantMessage newImportantMessage = importantMessages.get(last);
        importantMessages.remove(last);
        //updateRate(newImportantMessage.updateRate);

        return newImportantMessage;
    }

    private static class ImportantMessage
    {
        private final long expires;
        private final int delay_ticks;
        //private final int updateRate;
        private final Function<Void, String> callable;

        public ImportantMessage(Function<Void, String> msg, int ms_delay, Integer t_updateRate, boolean replaceable)
        {
            callable = msg;
            expires = System.currentTimeMillis() + ms_delay;
            //this.updateRate = t_updateRate==null ? DEFAULT_UPDATE_RATE : t_updateRate;
            this.delay_ticks = ms_delay*50;
        }

        public ImportantMessage(Function<Void, String> msg, int ms_delay, Integer t_updateRate)
        {
            callable = msg;
            expires = System.currentTimeMillis() + ms_delay;
            //this.updateRate = t_updateRate==null ? DEFAULT_UPDATE_RATE : t_updateRate;
            this.delay_ticks = ms_delay*50;
        }

        public boolean expired()
        {
            return System.currentTimeMillis() > expires;
        }

        public String getMessage()
        {
            return callable.apply(null);
        }
    }
}


