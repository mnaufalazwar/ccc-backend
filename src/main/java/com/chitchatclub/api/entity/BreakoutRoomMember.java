package com.chitchatclub.api.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "breakout_room_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"breakout_room_id", "user_id"})
})
public class BreakoutRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breakout_room_id")
    private BreakoutRoom breakoutRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public BreakoutRoomMember() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public BreakoutRoom getBreakoutRoom() {
        return breakoutRoom;
    }

    public void setBreakoutRoom(BreakoutRoom breakoutRoom) {
        this.breakoutRoom = breakoutRoom;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
