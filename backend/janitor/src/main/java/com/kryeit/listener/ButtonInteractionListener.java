package com.kryeit.listener;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.EnumSet;

public class ButtonInteractionListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("ticket")) {
            return;
        }

        event.deferReply(true).queue();

        final Guild guild = event.getGuild();
        if (guild == null) return;

        final Member member = event.getMember();
        if (member == null) return;

        Category category = guild.getCategoriesByName("tickets", true).stream().findFirst().orElse(null);
        createTicketChannel(guild, category, member, event);
    }

    private void createTicketChannel(Guild guild, Category category, Member member, ButtonInteractionEvent event) {

        int random4Digits = (int) (Math.random() * 9000) + 1000;
        final String channelName = "ticket-" + random4Digits;

        // Check if it already exists
        if (category.getTextChannels().stream().anyMatch(channel -> channel.getName().equals(channelName))) {
            createTicketChannel(guild, category, member, event);
            return;
        }

        category.createTextChannel(channelName).addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                .queue(channel -> {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("Ticket created")
                            .setDescription("""
                                    Please describe your issue here. Staff will be with you shortly.

                                    If you need to close the ticket, use the command /close.""")
                            .setFooter("Ticket ID: " + channel.getId())
                            .setColor(new Color(59, 152, 0));

                    channel.sendMessage(member.getAsMention()).setEmbeds(embed.build()).queue();

                    channel.createInvite().queue(invite -> {
                        event.getHook().sendMessage("Ticket created. You can access it here: " + invite.getUrl()).setEphemeral(true).queue();
                    });
                });
    }
}
