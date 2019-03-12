
package com.philleeran.flicktoucher.view.manage;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ManageActivity extends PreferenceActivity {

    private Fragment optionsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        optionsFragment = new ManageFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, optionsFragment).commit();
  //      setUpActionBar();
    }

//    private void setUpActionBar() {
   //     getActionBar().setDisplayShowHomeEnabled(false);
 //   }
}
