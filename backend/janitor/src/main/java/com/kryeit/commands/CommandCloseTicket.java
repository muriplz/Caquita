package com.kryeit.commands;

import com.kryeit.Ids;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.stream.Collectors;

public class CommandCloseTicket extends ListenerAdapter {

    public static void handle(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) return;

        Member member = event.getMember();
        if (member == null) return;

        boolean isMudWizard = member.getRoles().stream()
                .anyMatch(role -> role.getId().equals(Ids.Roles.MUD_WIZARD));

        Category category = guild.getCategoriesByName("tickets", true)
                .stream().findFirst().orElse(null);

        if (category == null) {
            event.reply("The 'tickets' category does not exist.").setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel currentChannel = (GuildMessageChannel) event.getChannel();

        if (isMudWizard && category.getChannels().contains(currentChannel)) {
            currentChannel.delete().queue();
            event.reply("Ticket closed by Mud Wizard.").setEphemeral(true).queue();
            return;
        }

        List<GuildMessageChannel> tickets = category.getChannels().stream()
                .filter(c -> c instanceof GuildMessageChannel)
                .map(c -> (GuildMessageChannel) c)
                .filter(c -> c.getPermissionContainer().getPermissionOverride(member) != null)
                .toList();

        if (tickets.contains(currentChannel)) {
            currentChannel.delete().queue();
        } else if (tickets.isEmpty()) {
            event.reply("You have no open tickets.").setEphemeral(true).queue();
        } else if (tickets.size() == 1) {
            tickets.get(0).delete().queue();
            event.reply("Your ticket has been closed.").setEphemeral(true).queue();
        } else {
            event.reply("Please specify which ticket you want to close.")
                    .addActionRow(tickets.stream()
                            .map(c -> Button.primary("close_ticket:" + c.getId(), "Close " + c.getName()))
                            .collect(Collectors.toList()))
                    .setEphemeral(true)
                    .queue();
        }
    }
}