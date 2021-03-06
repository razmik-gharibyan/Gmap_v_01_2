package com.example.gmap_v_01_2.repository.services.markers;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.gmap_v_01_2.editor.FollowerProcessing;
import com.example.gmap_v_01_2.editor.ImageProcessing;
import com.example.gmap_v_01_2.editor.ImageURLProcessing;
import com.example.gmap_v_01_2.repository.markers.repo.MarkersPoJo;
import com.example.gmap_v_01_2.repository.model.users.Markers;
import com.example.gmap_v_01_2.repository.model.users.UserDocumentAll;
import com.example.gmap_v_01_2.repository.services.firestore.model.UserDocument;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class UserMarkersService implements MarkerService {
    private final String TAG = getClass().toString();
    private Context context;
    private UserDocument userDocument;
    private ImageProcessing imageProcessing;
    private FollowerProcessing followersProcessing;
    private MarkersPoJo markersPoJo;

    public UserMarkersService(Context context) {
        this.context = context;
        userDocument = new UserDocument();
        followersProcessing = new FollowerProcessing();
        imageProcessing = new ImageProcessing(followersProcessing);
        markersPoJo = MarkersPoJo.getInstance();
    }

    @Nullable
    @Override
    public ArrayList<Integer> markersToBeRemoved(ArrayList<Markers> markerList, ArrayList<UserDocumentAll> listInBounds) {
        if (!markerList.isEmpty()) {
            //Remove from markerList those markers which don't exist anymore
            ArrayList<Markers> markerListTemp = new ArrayList<>();
            ArrayList<Integer> removable = new ArrayList<>();
            if(!listInBounds.isEmpty()) {
                for (int i = 0; i < markerList.size(); i++) {
                    boolean found = true;
                    for (int j = 0; j < listInBounds.size(); j++) {
                        if (markerList.get(i).getDocumentId().equals(listInBounds.get(j).getDocumentid())) {
                            if (listInBounds.get(j).getIsvisible()) {
                                double longitude = markerList.get(i).getLatLng().longitude;
                                double latitude = markerList.get(i).getLatLng().latitude;
                                if (longitude == listInBounds.get(j).getLocation().getLongitude() && latitude == listInBounds.get(j).getLocation().getLatitude()) {
                                    found = true;
                                    markerListTemp.add(markerList.get(i));
                                } else {
                                    found = false;
                                }
                            } else {
                                found = false;
                            }
                            break;
                        }
                        if (j == listInBounds.size() - 1) {
                            found = false;
                        }
                    }
                    if (!found) {
                        removable.add(i);
                    }
                }
            }else{
                for(int i=0; i<markerList.size(); i++) {
                    removable.add(i);
                    Log.d(TAG, "removable index is " + i);
                }
            }
            return removable;
        }
        return null;
    }

    @Nullable
    @Override
    public ArrayList<UserDocumentAll> markersToBeAdded(ArrayList<Markers> markerList, ArrayList<UserDocumentAll> listInBounds) {
        ArrayList<UserDocumentAll> list = new ArrayList<>();
        UserDocumentAll document;
        if(markerList.isEmpty()) {
            if(!listInBounds.isEmpty()) {
                return listInBounds;
            }
        }else{
            //Add markers from Firebase, if they do not exist on map
            for(int i = 0; i < listInBounds.size(); i++) {
                boolean found = false;
                for(int j = 0; j<markerList.size(); j++) {
                    if(listInBounds.get(i).getDocumentid().equals(markerList.get(j).getDocumentId())) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    document = new UserDocumentAll();
                    document.setDocumentid(listInBounds.get(i).getDocumentid());
                    document.setUsername(listInBounds.get(i).getUsername());
                    document.setPicture(listInBounds.get(i).getPicture());
                    document.setLocation(listInBounds.get(i).getLocation());
                    document.setFollowers(listInBounds.get(i).getFollowers());
                    document.setIsvisible(listInBounds.get(i).getIsvisible());
                    list.add(document);
                    Log.d("UserMarkersService", "user added to addable list");
                }
            }
            return list;
        }
        return null;
    }

    //TODO userLocation pojo -> to GeoPoint
    @Override
    public HashMap addMarker(String documentId, String userName, String userPicture, GeoPoint userLocation, Long userFollowers, boolean userVisible, boolean moveCamera) {
            if (userVisible) {
                HashMap markerParams = new HashMap();
                ImageURLProcessing imageURLProcessing = new ImageURLProcessing();
                imageURLProcessing.execute(userPicture);
                try {
                    Bitmap bitmap = imageURLProcessing.get();
                    MarkerOptions markerOptions = new MarkerOptions();
                    Bitmap roundBitMap;
                    Bitmap resizedBitMap;
                    Bitmap userListFragmentBitmap;
                    resizedBitMap = imageProcessing.getResizedBitmap(bitmap, userFollowers); // Resize bitmap
                    roundBitMap = imageProcessing.getCroppedBitmap(resizedBitMap); // Make current bitmap to round type
                    userListFragmentBitmap = imageProcessing.getCroppedBitmap(imageProcessing.getResizedBitmapForUserListFragment(bitmap)); // Make current bitmap for userlist fragment type
                    String userPictureString = imageProcessing.bitmapToString(userListFragmentBitmap); //Convert bitmap to String to send to fragment as param
                    String fullPictureString = imageProcessing.bitmapToString(bitmap);

                    //TODO read current location data from shared prefs
                    LatLng userLongLat = new LatLng(1, 1);

                    if (userLocation != null) {
                        userLongLat = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                    }
                    markerOptions.position(userLongLat);
                    markerOptions.visible(userVisible);
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(roundBitMap));
                    String currentFollowers = followersProcessing.instagramFollowersType(userFollowers);
                    markerOptions.title(userName + " : " + currentFollowers + " Followers");
                    LatLng finalUserLongLat = userLongLat;
                    ArrayList<String> usernameList;
                    ArrayList<String> userpictureList;
                    ArrayList<String> userfollowersList;
                    ArrayList<String> userfullpicture;
                    usernameList = markersPoJo.getUsernameList();
                    usernameList.add(userName);
                    markersPoJo.setUsernameList(usernameList);
                    userpictureList = markersPoJo.getUserpictureList();
                    userpictureList.add(userPictureString);
                    markersPoJo.setUserpictureList(userpictureList);
                    userfollowersList = markersPoJo.getUserfollowersList();
                    userfollowersList.add(currentFollowers);
                    markersPoJo.setUserfollowersList(userfollowersList);
                    userfullpicture = markersPoJo.getUserfullpicture();
                    userfullpicture.add(fullPictureString);
                    markersPoJo.setUserfullpicture(userfullpicture);
                    markerParams.put("markerOptions", markerOptions);
                    markerParams.put("documentId", documentId);
                    markerParams.put("LongLat", finalUserLongLat);
                    markerParams.put("moveCamera", moveCamera);
                    return markerParams;

                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return null;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }else{
                return null;
            }
    }
}
