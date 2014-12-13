package sk.ab.herbs.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import sk.ab.herbs.Constants;
import sk.ab.herbs.PlantHeader;
import sk.ab.herbs.R;
import sk.ab.herbs.activities.DisplayPlantActivity;
import sk.ab.tools.DrawableManager;
import sk.ab.tools.GetResource;

import java.util.List;

public class PlantListFragment extends ListFragment {

    static class ViewHolder {
        TextView title;
        TextView family;
        ImageView familyIcon;
        ImageView photo;
    }

    public class PropertyAdapter extends ArrayAdapter<PlantHeader> {

        public PropertyAdapter(Context context) {
            super(context, 0);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            PlantHeader plantHeader = getItem(position);

            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.plant_row, null);
                viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.plant_title);
                viewHolder.family = (TextView) convertView.findViewById(R.id.plant_family);
                viewHolder.familyIcon = (ImageView) convertView.findViewById(R.id.family_icon);
                viewHolder.photo = (ImageView) convertView.findViewById(R.id.plant_photo);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.photo.setImageResource(android.R.color.transparent);

            if (plantHeader.getUrl() != null) {
                DrawableManager.getDrawableManager().fetchDrawableOnThread(plantHeader.getUrl(), viewHolder.photo);
            }

            viewHolder.title.setText(plantHeader.getTitle());
            viewHolder.family.setText(plantHeader.getFamily());
            viewHolder.familyIcon.setImageResource(GetResource.getResourceDrawable(Constants.FAMILY +
                    Constants.RESOURCE_SEPARATOR + plantHeader.getFamilyId(), getActivity().getBaseContext(),
                    R.drawable.home));

            return convertView;
        }
    }

    private PropertyAdapter adapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new PropertyAdapter(getActivity());
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        DisplayPlantActivity activity = (DisplayPlantActivity) getActivity();
        activity.setPlantHeader(activity.getPlants().get(position));
        activity.getDetailResponder().getDetail();
        activity.getDrawerLayout().closeDrawers();
        activity.getDrawerToggle().syncState();
    }

    public void recreateList(List<PlantHeader> plants) {
        adapter.clear();
        for (PlantHeader plantHeader : plants) {
            adapter.add(plantHeader);
        }
    }
}