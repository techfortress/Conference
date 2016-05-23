package me.robomwm.Conference;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by robom on 5/22/2016.
 */
public class ConferenceManager
{
    private Map<String, ConferenceRoom> conferenceRooms = new HashMap<>();
    private Map<Player, ConferenceParticipant> conferenceParticipants = new HashMap<>();

    /**
     * Gets a stored ConferenceRoom
     * @param roomString
     * @return null if the room does not exist
     */
    public ConferenceRoom getConferenceRoom(String roomString)
    {
        return this.conferenceRooms.get(roomString.toLowerCase());
    }

    /**
     * Adds a player to a conference room, and creates one if the room doesn't exist
     * removes player from their prior conference room, if in one
     * Broadcasts join to all conference participants
     * @param player
     * @param roomString
     * @return -1 if the room did not exist and had to be created, 0 if player was not invited, 1 if successful
     */
    public int addParticipant(Player player, String roomString)
    {
        roomString = roomString.toLowerCase();

        //check for existence of specified room, and create if it doesn't exist
        if (!this.conferenceRooms.containsKey(roomString))
        {
            this.conferenceRooms.put(roomString, new ConferenceRoom(roomString, player));
            this.conferenceParticipants.put(player, new ConferenceParticipant(this.getRoom(roomString)));
            return -1;
        }

        ConferenceRoom room = this.conferenceRooms.get(roomString);

        //Check if invited
        if (!room.isInvited(player))
            return 0;

        //Otherwise, add and broadcast
        removeParticipant(player, true);
        room.addParticipant(player);
        room.sendBroadcast(player.getName() + " joined the conference room.");
        this.conferenceParticipants.put(player, new ConferenceParticipant(this.getRoom(roomString)));
        return 1;
    }

    /**
     * Returns the conference room the player is in
     * @param player
     * @return null if it can't find stuff
     */
    public ConferenceRoom getParticipantRoom(Player player)
    {
        return conferenceParticipants.get(player).getConferenceRoom();
    }

    public ConferenceRoom getRoom(String room)
    {
        return conferenceRooms.get(room);
    }

    /**
     * Removes a player from a conference room
     * Also deletes the conference room, if empty
     * @param player
     * @return false if player was not a participant
     */
    public boolean removeParticipant(Player player, boolean broadcastRemove)
    {
        if (this.conferenceParticipants.containsKey(player))
        {
            ConferenceRoom room = this.conferenceRooms.get(this.conferenceParticipants.get(player));
            if (broadcastRemove)
                room.sendBroadcast(player.getName() + " left the conference room.");
            room.removeParticipant(player);
            removeRoomIfEmpty(room.getName());
            this.conferenceParticipants.remove(player);
            return true;
        }
        else
            return false;
    }

    /**
     * Used internally
     * Removes a room if nobody is inside it.
     */
    public void removeRoomIfEmpty(String roomString)
    {
        if (this.conferenceRooms.get(roomString).isEmpty())
            this.conferenceRooms.remove(roomString);
    }

    /**
     * Removes a conference room and its participants
     * @param roomString
     * @return false if room doesn't exist
     */
    public boolean removeConferenceRoom(String roomString)
    {
        roomString = roomString.toLowerCase();
        if (!this.conferenceRooms.containsKey(roomString))
            return false;

        ConferenceRoom room = this.conferenceRooms.get(roomString);

        for (Player participant : room.getParticipants())
            this.removeParticipant(participant, false);

        this.conferenceRooms.remove(roomString);
    }
}
