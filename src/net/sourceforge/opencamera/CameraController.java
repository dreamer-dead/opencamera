package net.sourceforge.opencamera;

import java.util.List;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

class CameraControllerException extends Exception {
	private static final long serialVersionUID = 7904697847749213106L;

};

public abstract class CameraController {
	private static final String TAG = "CameraController";

	// for testing:
	public int count_camera_parameters_exception = 0;

	static class CameraFeatures {
		boolean is_zoom_supported = false;
		int max_zoom = 0;
		List<Integer> zoom_ratios = null;
		boolean supports_face_detection = false;
		List<CameraController.Size> picture_sizes = null;
		List<CameraController.Size> video_sizes = null;
		List<CameraController.Size> preview_sizes = null;
		List<String> supported_flash_values = null;
		List<String> supported_focus_values = null;
		int max_num_focus_areas = 0;
		float minimum_focus_distance = 0.0f;
		boolean is_exposure_lock_supported = false;
		boolean is_video_stabilization_supported = false;
		boolean supports_iso_range = false;
		int min_iso = 0;
		int max_iso = 0;
		boolean supports_exposure_time = false;
		long min_exposure_time = 0l;
		long max_exposure_time = 0l;
		int min_exposure = 0;
		int max_exposure = 0;
		float exposure_step = 0.0f;
		boolean can_disable_shutter_sound = false;
	}

	public static class Size {
		public int width = 0;
		public int height = 0;
		
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		public boolean equals(Size that) {
			return this.width == that.width && this.height == that.height;
		}
	}
	
	static class Area {
		public Rect rect = null;
		public int weight = 0;
		
		public Area(Rect rect, int weight) {
			this.rect = rect;
			this.weight = weight;
		}
	}
	
	static interface FaceDetectionListener {
		public abstract void onFaceDetection(Face[] faces);
	}
	
	static interface PictureCallback {
		public abstract void onPictureTaken(byte[] data);
	}
	
	static interface AutoFocusCallback {
		public abstract void onAutoFocus(boolean success);
	}
	
	static interface ErrorCallback {
		public abstract void onError();
	}
	
	static class Face {
		public int score = 0;
		public Rect rect = null;

		Face(int score, Rect rect) {
			this.score = score;
			this.rect = rect;
		}
	}
	
	class SupportedValues {
		List<String> values = null;
		String selected_value = null;
		SupportedValues(List<String> values, String selected_value) {
			this.values = values;
			this.selected_value = selected_value;
		}
	}

	abstract void release();

	abstract String getAPI();
	abstract CameraFeatures getCameraFeatures();
	abstract SupportedValues setSceneMode(String value);
	public abstract String getSceneMode();
	abstract SupportedValues setColorEffect(String value);
	public abstract String getColorEffect();
	abstract SupportedValues setWhiteBalance(String value);
	public abstract String getWhiteBalance();
	abstract SupportedValues setISO(String value);
    abstract String getISOKey();
	abstract int getISO();
	abstract boolean setISO(int iso);
	abstract long getExposureTime();
	abstract boolean setExposureTime(long exposure_time);
    public abstract CameraController.Size getPictureSize();
    abstract void setPictureSize(int width, int height);
    public abstract CameraController.Size getPreviewSize();
    abstract void setPreviewSize(int width, int height);
	abstract void setVideoStabilization(boolean enabled);
	public abstract boolean getVideoStabilization();
	abstract public int getJpegQuality();
	abstract void setJpegQuality(int quality);
	abstract public int getZoom();
	abstract void setZoom(int value);
	abstract int getExposureCompensation();
	abstract boolean setExposureCompensation(int new_exposure);
	abstract void setPreviewFpsRange(int min, int max);
	abstract List<int []> getSupportedPreviewFpsRange();

	public abstract String getDefaultSceneMode();
	public abstract String getDefaultColorEffect();
	public abstract String getDefaultWhiteBalance();
	public abstract String getDefaultISO();
	public abstract long getDefaultExposureTime();

	abstract void setFocusValue(String focus_value);
	abstract public String getFocusValue();
	abstract void setFocusDistance(float focus_distance);
	abstract void setFlashValue(String flash_value);
	abstract public String getFlashValue();
	abstract void setRecordingHint(boolean hint);
	abstract void setAutoExposureLock(boolean enabled);
	abstract public boolean getAutoExposureLock();
	abstract void setRotation(int rotation);
	abstract void setLocationInfo(Location location);
	abstract void removeLocationInfo();
	abstract void enableShutterSound(boolean enabled);
	abstract boolean setFocusAndMeteringArea(List<CameraController.Area> areas);
	abstract void clearFocusAndMetering();
	public abstract List<CameraController.Area> getFocusAreas();
	public abstract List<CameraController.Area> getMeteringAreas();
	abstract boolean supportsAutoFocus();
	abstract boolean focusIsVideo();
	abstract void reconnect() throws CameraControllerException;
	abstract void setPreviewDisplay(SurfaceHolder holder) throws CameraControllerException;
	abstract void setPreviewTexture(SurfaceTexture texture) throws CameraControllerException;
	abstract void startPreview() throws CameraControllerException;
	abstract void stopPreview();
	public abstract boolean startFaceDetection();
	abstract void setFaceDetectionListener(final CameraController.FaceDetectionListener listener);
	abstract void autoFocus(final CameraController.AutoFocusCallback cb);
	abstract void cancelAutoFocus();
	abstract void takePicture(final CameraController.PictureCallback raw, final CameraController.PictureCallback jpeg, final ErrorCallback error);
	abstract void setDisplayOrientation(int degrees);
	abstract int getDisplayOrientation();
	abstract int getCameraOrientation();
	abstract boolean isFrontFacing();
	abstract void unlock();
	abstract void initVideoRecorderPrePrepare(MediaRecorder video_recorder);
	abstract void initVideoRecorderPostPrepare(MediaRecorder video_recorder) throws CameraControllerException;
	abstract String getParametersString();
	boolean captureResultHasIso() {
		return false;
	}
	int captureResultIso() {
		return 0;
	}

	// gets the available values of a generic mode, e.g., scene, color etc, and makes sure the requested mode is available
	protected SupportedValues checkModeIsSupported(List<String> values, String value, String default_value) {
		if( values != null && values.size() > 1 ) { // n.b., if there is only 1 supported value, we also return null, as no point offering the choice to the user (there are some devices, e.g., Samsung, that only have a scene mode of "auto")
			if( MyDebug.LOG ) {
				for(int i=0;i<values.size();i++) {
		        	Log.d(TAG, "supported value: " + values.get(i));
				}
			}
			// make sure result is valid
			if( !values.contains(value) ) {
				if( MyDebug.LOG )
					Log.d(TAG, "value not valid!");
				if( values.contains(default_value) )
					value = default_value;
				else
					value = values.get(0);
				if( MyDebug.LOG )
					Log.d(TAG, "value is now: " + value);
			}
			return new SupportedValues(values, value);
		}
		return null;
	}
}
