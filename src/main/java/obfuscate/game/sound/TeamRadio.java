package obfuscate.game.sound;

import obfuscate.game.core.Game;
import obfuscate.team.StrikeTeam;
import org.bukkit.Sound;

/** Intercom for one team */
public class TeamRadio {

    private Game _game;
    private StrikeTeam _team;

    public TeamRadio(Game game, StrikeTeam team) {
        this._game = game;
        this._team = team;
    }

    public void play(Sound s) {
        _game.getSoundManager().playSound(s).forTeam(_team).soundDistance(1000).play();
    }

    public void play(Radio s) {
        _game.getSoundManager().playSound(s.getSound()).forTeam(_team).soundDistance(1000).play();
    }

    public void roundStart() {
        if (_team == StrikeTeam.CT) {
            _game.getSoundManager().ctRoundStart().soundDistance(1000).forTeam(_team).play();
        } else {
            _game.getSoundManager().tRoundStart().soundDistance(1000).forTeam(_team).play();
        }
    }

    public void roundWin() {
        if (_team == StrikeTeam.CT) {
            _game.getSoundManager().announceCTWin().forTeam(_team).soundDistance(1000).play();
        } else {
            _game.getSoundManager().announceTWin().forTeam(_team).soundDistance(1000).play();
        }
    }

    public void roundLoose() {
        if (_team == StrikeTeam.CT) {
            _game.getSoundManager().announceTWin().forTeam(_team).soundDistance(1000).play();
        } else {
            _game.getSoundManager().announceCTWin().forTeam(_team).soundDistance(1000).play();
        }
    }

    public void bombDrop() {
        _game.getSoundManager().dropBomb().forTeam(_team).soundDistance(1000).play();
    }
}
