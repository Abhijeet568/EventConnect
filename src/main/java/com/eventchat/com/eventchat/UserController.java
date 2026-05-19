package com.eventchat;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileRepository userProfileRepo;
    private final ChatRoomRepository chatRoomRepo;
    private final UserHistoryRepository historyRepo;

    public UserController(UserProfileRepository userProfileRepo, ChatRoomRepository chatRoomRepo, UserHistoryRepository historyRepo) {
        this.userProfileRepo = userProfileRepo;
        this.chatRoomRepo = chatRoomRepo;
        this.historyRepo = historyRepo;
    }

    @PostMapping("/login/{username}")
    public UserProfile loginUser(@PathVariable String username) {
        return userProfileRepo.findById(username).orElseGet(() -> {
            UserProfile p = new UserProfile(username);
            userProfileRepo.save(p);
            historyRepo.save(new UserHistory(username, "Account Created"));
            return p;
        });
    }

    @GetMapping("/{username}")
    public Map<String, Object> getUserProfile(@PathVariable String username) {
        UserProfile profile = userProfileRepo.findById(username).orElse(new UserProfile(username));
        List<ChatRoom> myRooms = chatRoomRepo.findByCreator(username);
        profile.setRoomsCreated(myRooms.size());
        userProfileRepo.save(profile);

        Map<String, Object> response = new HashMap<>();
        response.put("profile", profile);
        response.put("myRooms", myRooms);
        response.put("history", historyRepo.findByUsernameOrderByTimestampDesc(username));

        // Get all members of rooms this user has joined
        List<Map<String, Object>> joinedRoomsData = new ArrayList<>();
        List<UserProfile> allUsers = userProfileRepo.findAll();

        for (String roomName : profile.getJoinedRooms()) {
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("roomName", roomName);
            List<String> participants = allUsers.stream()
                    .filter(u -> u.getJoinedRooms().contains(roomName))
                    .map(UserProfile::getUsername)
                    .collect(Collectors.toList());
            roomData.put("participants", participants);
            joinedRoomsData.add(roomData);
        }
        response.put("joinedRoomsData", joinedRoomsData);

        return response;
    }

    @PostMapping("/{username}/connect/{target}")
    public boolean connectUser(@PathVariable String username, @PathVariable String target) {
        Optional<UserProfile> optUser = userProfileRepo.findById(username);
        if (optUser.isPresent() && !username.equals(target)) {
            UserProfile user = optUser.get();
            user.getConnections().add(target);
            userProfileRepo.save(user);
            historyRepo.save(new UserHistory(username, "Connected with " + target));
            return true;
        }
        return false;
    }
}