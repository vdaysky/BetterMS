package obfuscate.util.chat;

public class Format {


    public static String Health(double health, double maxHealth, int heartCount) {
        char heart = '❤';

        float healthPercent = (float) (health / maxHealth);

        int fullHearts = (int) Math.floor(healthPercent * heartCount);
        int emptyHearts = heartCount - fullHearts;
        boolean addHalfHeart = false;
        if ((healthPercent * heartCount) - Math.floor(healthPercent * heartCount) > 0.5f) {
            addHalfHeart = true;
            emptyHearts--;
        }

        StringBuilder healthBar = new StringBuilder();

        healthBar.append(C.cRed);

        for (int i = 0; i < fullHearts; i++) {
            healthBar.append(heart);
        }
        if (addHalfHeart) {
            healthBar.append(C.cGray).append(heart);
        }

        for (int i = 0; i < emptyHearts; i++) {
            healthBar.append(C.cDGray).append(heart);
        }

        return healthBar.toString();
    }

}
