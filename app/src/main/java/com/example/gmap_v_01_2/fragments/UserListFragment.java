package com.example.gmap_v_01_2.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gmap_v_01_2.OnSwipeTouchListener;
import com.example.gmap_v_01_2.R;
import com.example.gmap_v_01_2.recyclerview.UserAdapter;
import com.example.gmap_v_01_2.recyclerview.User_Item;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    //RecyclerView classes
    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    private ArrayList<User_Item> useritemsList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    public UserListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        useritemsList.clear();
        //Get Parameters from Activity
        Bundle arguments = getArguments();

        if (arguments != null) {
            List<String> username = arguments.getStringArrayList("username");
            List<String> userpic = arguments.getStringArrayList("userpicture");
            List<String> userfols = arguments.getStringArrayList("userfollowers");

            if(userfols != null && userpic != null && username != null) {
                for(int i = 0; i < username.size(); i++)
                    useritemsList.add(new User_Item(userpic.get(i), username.get(i), userfols.get(i)));
            }

        }

        //Initialize RecyclerView
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new UserAdapter(useritemsList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //This is if user clicked on whole line of RecyclerViews line in position "position"
                Log.d("recycler item click: ", "item clicked by position: " + position);
            }

            @Override
            public void onPictureClick(int position) {
                //This is if user clicked on picture of user in line of RecyclerView line in position "position"
                mListener.onFragmentInteraction(false,true,position);
            }
        });

        mRecyclerView.setOnTouchListener(new OnSwipeTouchListener(this.getContext()) {
            @Override
            public void onSwipeRight() {
                Log.d("touch event: ", "swiped to right");
            }

            @Override
            public void onSwipeLeft() {
                mListener.onFragmentInteraction(true, false, 0);
                Log.d("touch event: ", "swiped to left");
            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Boolean bool,Boolean openPhotoFragment, int pos);
    }

}