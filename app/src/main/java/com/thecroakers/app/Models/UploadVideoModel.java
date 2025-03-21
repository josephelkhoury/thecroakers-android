package com.thecroakers.app.Models;

import java.io.Serializable;

public class UploadVideoModel implements Serializable {
    String userId,soundId,description,privacyPolicy,allowComments,allowDuet,hashtagsJson,usersJson
            ,videoId,duet, mainVideoId, topicId, countryId;

    public UploadVideoModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSoundId() {
        return soundId;
    }

    public void setSoundId(String soundId) {
        this.soundId = soundId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    public String getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(String allowComments) {
        this.allowComments = allowComments;
    }

    public String getAllowDuet() {
        return allowDuet;
    }

    public void setAllowDuet(String allowDuet) {
        this.allowDuet = allowDuet;
    }

    public String getHashtagsJson() {
        return hashtagsJson;
    }

    public void setHashtagsJson(String hashtagsJson) {
        this.hashtagsJson = hashtagsJson;
    }

    public String getUsersJson() {
        return usersJson;
    }

    public void setUsersJson(String usersJson) {
        this.usersJson = usersJson;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getMainVideoId() {
        return mainVideoId;
    }

    public void setMainVideoId(String mainVideoId) {
        this.mainVideoId = mainVideoId;
    }

    public String getDuet() {
        return duet;
    }

    public void setDuet(String duet) {
        this.duet = duet;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }
}