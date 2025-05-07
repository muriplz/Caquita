package com.kryeit.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;

import java.util.List;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "kofi", "donate" -> CommandKofi.handle(event);
            case "ticket" -> CommandTicket.handle(event);
            case "close" -> CommandCloseTicket.handle(event);

        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        AutoCompleteQuery focusedOption = event.getFocusedOption();
        String value = event.getFocusedOption().getValue();

        List<String> suggestions = switch (focusedOption.getName()) {
         //   case "playername" -> Utils.getNameSuggestions(value);
         //  case "banned-playername" -> Utils.getBannedNameSuggestions(value);
         //   case "duration" -> DurationParser.suggestDuration(value);
            default -> List.of();
        };

        event.replyChoiceStrings(suggestions).queue();
    }
}
