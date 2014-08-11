package sk.ab.herbs.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import sk.ab.herbs.*;
import sk.ab.herbs.fragments.PlantListFragment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: adrian
 * Date: 28.2.2013
 * Time: 18:00
 * To change this template use File | Settings | File Templates.
 */
public class ListPlantsActivity extends ActionBarActivity {
  private PlantListFragment plantsFragment;

  private List<PlantHeader> plants;

  /**
   * Called when the commons is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set the Content View
    setContentView(R.layout.list_frame);
    plantsFragment = new PlantListFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.list_frame, plantsFragment)
        .commit();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onStart() {
    super.onStart();
    plants = getIntent().getExtras().getParcelableArrayList("results");

    plantsFragment.recreateList(plants);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.result_list, menu);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    MenuItem item = menu.findItem(R.id.count);
    Button b = (Button)item.getActionView().findViewById(R.id.countButton);
    b.setText(""+plants.size());

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  public List<PlantHeader> getPlants() {
    return plants;
  }
}
