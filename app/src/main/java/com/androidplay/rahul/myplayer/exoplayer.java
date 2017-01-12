package com.androidplay.rahul.myplayer;

/**
 * Created by Rahul on 20-07-2016.
 */
public class exoplayer {

   //   MediaCodecAudioTrackRenderer <- ExtractorSampleSource<-DefaultUriDataSource>

/*
Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE/1024);
DataSource dataSource = new DefaultUriDataSource(context, null, userAgent);
ExtractorSampleSource sampleSource = new ExtractorSampleSource(
    uri, dataSource, allocator, BUFFER_SEGMENT_COUNT/1 * BUFFER_SEGMENT_SIZE);
MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(
    context, sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(
    sampleSource, MediaCodecSelector.DEFAULT);
 */

    /*

    // 1. Instantiate the player.
 ExoPlayer player = ExoPlayer.Factory.newInstance(RENDERER_COUNT);
 // 2. Construct renderers.
 MediaCodecVideoTrackRenderer videoRenderer = ...
 MediaCodecAudioTrackRenderer audioRenderer = ...
 // 3. Inject the renderers through prepare.
 player.prepare(videoRenderer, audioRenderer);
 // 4. Pass the surface to the video renderer.
 player.sendMessage(videoRenderer, MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, surface);
 // 5. Start playback.
 player.setPlayWhenReady(true);
 ...
 player.release(); // Don’t forget to release when done!
     */
}
