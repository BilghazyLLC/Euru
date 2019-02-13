package io.euruapp.core.location;

import android.location.Location;

/**
 * Obtained from {@link "http://gabesechansoftware.com/location-tracking/"}
 */
public interface LocationTracker {
	public interface LocationUpdateListener {
		public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime);
	}
	
	public void start();
	
	public void start(LocationUpdateListener update);
	
	public void stop();
	
	public boolean hasLocation();
	
	public boolean hasPossiblyStaleLocation();
	
	public Location getLocation();
	
	public Location getPossiblyStaleLocation();
	
}
