package de.legend.legendperms;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Created by YannicK S. on 26.05.2023
 */
public class LanguageManagerTest {

    private LegendPermsPlugin plugin;
    private ServerMock server;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(LegendPermsPlugin.class);
        this.player = this.server.addPlayer();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void sendTranslatedMessage() {
        final String identifier = "rank_expired";
        this.player.nextMessage(); //skip join message
        this.plugin.languageManager().sendTranslatedMessage(this.player, identifier);
        Assertions.assertEquals("§cYour rank has expired. You are now in the rank %PH%", this.player.nextMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "test2", "test3"})
    public void sendTranslatedMessageReplacePlaceholder(String placeholder) {
        final String identifier = "rank_expired";
        this.player.nextMessage(); //skip join message
        this.plugin.languageManager().sendTranslatedMessage(this.player, identifier, placeholder);
        Assertions.assertEquals("§cYour rank has expired. You are now in the rank " + placeholder,
                this.player.nextMessage());
    }

    @Test
    public void sendTranslatedMessageWrongIdentifier() {
        final String identifier = "wrong_identifier";
        this.player.nextMessage(); //skip join message
        this.plugin.languageManager().sendTranslatedMessage(this.player, identifier);
        Assertions.assertEquals("error -> wrong_identifier not set!", this.player.nextMessage());
    }


}
