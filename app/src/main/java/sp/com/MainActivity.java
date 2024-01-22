package sp.com;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private InventoryFragment inventoryFragment;
    private BottomNavigationView navView;
    private QRCodeFragment qrCodeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        navView = findViewById(R.id.bottom_navigation);
        navView.setOnItemSelectedListener(menuSelected);

        // Initialise the fragments
        homeFragment = new HomeFragment();
        inventoryFragment = new InventoryFragment();
        qrCodeFragment = new QRCodeFragment();

        // Load the default fragment when the app starts
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFragment).commit();

    }

    private final NavigationBarView.OnItemSelectedListener menuSelected = new BottomNavigationView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.navigation_inventory) {
                selectedFragment = inventoryFragment;
            } else if (itemId == R.id.navigation_scan) {
                selectedFragment = qrCodeFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        }
    };
}