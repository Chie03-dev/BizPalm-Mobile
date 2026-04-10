package project.bizpalm.ui.dashboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NearbyStoresActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // This approach directly opens Google Maps with the requested search terms.
        // It's more reliable and provides full navigation features.
        openGoogleMapsWithSearch();
        
        // Close this activity immediately so the user returns to the previous screen 
        // when they back out of Google Maps.
        finish();
    }

    private void openGoogleMapsWithSearch() {
        // Define the search categories requested: Markets, Stores, Supermarkets, and food vendors
        String searchQuery = "Stores and Supermarkets";
        
        // URI format: geo:0,0?q=search+terms
        // Google Maps will automatically use the device's GPS to find these near the user.
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(searchQuery));
        
        // Create the Intent to view the Map
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        
        // Try to force the official Google Maps app if it's installed
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            // Check if there is an app available to handle the intent
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: Open in the web browser if the Google Maps app is not available
                Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(searchQuery));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Maps", Toast.LENGTH_SHORT).show();
        }
    }
}
