package com.wozzytheprogrammer.kwproperty.Customer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cedarsoftware.util.io.JsonReader;
import com.wozzytheprogrammer.kwproperty.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PropertyAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PropertyAdapter";
    public static final int VIEW_TYPE_EMPTY = 0;
    public static final int VIEW_TYPE_NORMAL = 1;

    private Callback mCallback;
    private List<Properties> mPropertiesList;
    private List<Properties> mJsonPropertiesList;
    private JSONObject parseObj;

    public PropertyAdapter(List<Properties> propertiesList) {
        mPropertiesList = propertiesList;
        mJsonPropertiesList = propertiesList;

    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
            case VIEW_TYPE_EMPTY:
            default:
                return new EmptyViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_empty_view, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mJsonPropertiesList != null && mJsonPropertiesList.size() > 0) {
            return VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @Override
    public int getItemCount() {
       getJSON("https://www.wozzytheprogrammer.com/objectApi.php");
        if (mJsonPropertiesList != null && mJsonPropertiesList.size() > 0) {
            Log.e("size", String.valueOf(parseObj));
            return mJsonPropertiesList.size();
        } else {
            return 1;
        }
    }


    public void addItems(List<Properties> propertiesList) {
        mPropertiesList.addAll(propertiesList);
        notifyDataSetChanged();
    }

    public interface Callback {
        void onEmptyViewRetryClick();
    }

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.thumbnail)
        ImageView coverImageView;

        @BindView(R.id.title)
        TextView titleTextView;

        @BindView(R.id.newsTitle)
        TextView newsTextView;

        @BindView(R.id.newsInfo)
        TextView infoTextView;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        protected void clear() {
            coverImageView.setImageDrawable(null);
            titleTextView.setText("");
            newsTextView.setText("");
            infoTextView.setText("");
        }

        public void onBind(int position) {
            super.onBind(position);

            final Properties mProperties = mPropertiesList.get(position);

            if (mProperties.getImageUrl() != null) {
                Glide.with(itemView.getContext())
                        .load(mProperties.getImageUrl())
                        .into(coverImageView);
            }

            if (mProperties.getTitle() != null) {
                titleTextView.setText(mProperties.getTitle());
            }

            if (mProperties.getSubTitle() != null) {
                newsTextView.setText(mProperties.getSubTitle());
            }

            if (mProperties.getInfo() != null) {
                infoTextView.setText(mProperties.getInfo());
            }

            itemView.setOnClickListener(v -> {
                if (mProperties.getImageUrl() != null) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        intent.setData(Uri.parse(mProperties.getImageUrl()));
                        itemView.getContext().startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "onClick: Image url is not correct");
                    }
                }
            });
        }
    }

    public class EmptyViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_message)
        TextView messageTextView;
        @BindView(R.id.buttonRetry)
        TextView buttonRetry;

        EmptyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            buttonRetry.setOnClickListener(v -> mCallback.onEmptyViewRetryClick());
        }

        @Override
        protected void clear() {

        }

    }

    /**
     Calls the backend api and loads json data from it....
     */
    private void getJSON(final String urlWebService) {

        class GetJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
//                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                try {
                    parseJsonInfo(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //in this method we are fetching the json string
            @Override
            protected String doInBackground(Void... voids) {


                try {
                    //creating a URL
                    URL url = new URL(urlWebService);

                    //Opening the URL using HttpURLConnection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    //StringBuilder object to read the string from the service
                    StringBuilder sb = new StringBuilder();

                    //We will use a buffered reader to read the string from service
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    //A simple string to read values from each line
                    String json;

                    //reading until we don't find null
                    while ((json = bufferedReader.readLine()) != null) {

                        //appending it to string builder
                        sb.append(json + "\n");
                    }

                    //finally returning the read string
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }

            }
        }

        //creating asynctask object and executing it
        GetJSON getJSON = new GetJSON();
        getJSON.execute();


    }
    /**
     Parses the Json when its returned, and sets markers on the maps
     */
    private void parseJsonInfo(String json) throws JSONException {
        //creating a json array from the json string
        JSONArray addressArray = new JSONArray(json);
        //creating a string array for listview
        String[] addresses = new String[addressArray.length()];
        String[] imageUrl = new String[addressArray.length()];
        String[] propertyInformation = new String[addressArray.length()];
        String[] finalArray =  new String[addressArray.length()];



        //looping through all the elements in json array
        for (int i = 0; i < addressArray.length(); i++) {

            JSONObject obj = addressArray.getJSONObject(i);
            Log.e("thdddddsa", obj.toString());
            addresses[i] = obj.getString("address");
            propertyInformation[i] = obj.getString("information");
            imageUrl[i] = obj.getString("imgUrl");
            Object result =  JsonReader.jsonToJava(String.valueOf(addressArray));
            Log.e("result", String.valueOf(result));

        }
    }
}
