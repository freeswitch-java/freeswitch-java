package com.freeswitchjava.esl.api;

/**
 * FreeSWITCH {@code uuid_video_refresh} API command — requests a video keyframe from the remote end.
 *
 * <pre>{@code
 * client.api(new VideoRefreshCommand("uuid-abc"));
 * }</pre>
 */
public final class VideoRefreshCommand implements EslApiCommand {

    private final String uuid;

    public VideoRefreshCommand(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toApiString() {
        return "uuid_video_refresh " + uuid;
    }
}
