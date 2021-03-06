package sk.ab.herbsplus.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import sk.ab.common.entity.FirebasePlant;
import sk.ab.common.entity.PlantHeader;
import sk.ab.herbsbase.AndroidConstants;
import sk.ab.herbsbase.BaseApp;
import sk.ab.herbsbase.tools.SynchronizedCounter;
import sk.ab.herbsbase.tools.Utils;
import sk.ab.herbsplus.SpecificConstants;

/**
 * Service to synchronize offline photos and family's icons
 *
 * Created by adrian on 2/4/2018.
 */

public class OfflineService extends JobIntentService {

    public static final int JOB_ID = 1;

    private static final String TAG = "OfflineService";
    private static final String FIRST_FLOWER = "Acer campestre";
    private static final String FIRST_FAMILY = "Acanthaceae";

    private FirebaseDatabase database;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, OfflineService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@Nullable Intent intent) {
        if (!BaseApp.isConnectedToWifi(getApplicationContext())) {
            return;
        }

        database = FirebaseDatabase.getInstance();

        boolean offlineMode = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE).getBoolean(SpecificConstants.OFFLINE_MODE_KEY, false);
        if (offlineMode) {
            // download photos and family icons
            DatabaseReference mFirebaseRefCount = database.getReference(AndroidConstants.FIREBASE_PLANTS_TO_UPDATE);
            mFirebaseRefCount.keepSynced(true);
            final Query queryCount = mFirebaseRefCount.child(AndroidConstants.FIREBASE_DATA_COUNT);
            mFirebaseRefCount.child("refreshMock").setValue("mock", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    queryCount.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            assert dataSnapshot.getValue() != null;
                            final Integer countAll = ((Long) dataSnapshot.getValue()).intValue();

                            DatabaseReference mFirebaseRefPlant = database.getReference(AndroidConstants.FIREBASE_PLANTS + AndroidConstants.SEPARATOR + FIRST_FLOWER);
                            mFirebaseRefPlant.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    FirebasePlant plant = dataSnapshot.getValue(FirebasePlant.class);

                                    assert plant != null;
                                    File photoIllustration = new File(OfflineService.this.getApplicationContext().getFilesDir()
                                            + AndroidConstants.SEPARATOR + AndroidConstants.STORAGE_PHOTOS + plant.getIllustrationUrl());
                                    if (photoIllustration.exists()) {
                                        int from = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE).getInt(SpecificConstants.OFFLINE_PLANT_KEY, -1);
                                        synchronizePlant(from + 1, countAll);
                                    } else {
                                        synchronizePlant(0, countAll);
                                    }

                                    File familyIcon = new File(OfflineService.this.getApplicationContext().getFilesDir()
                                            + AndroidConstants.SEPARATOR + AndroidConstants.STORAGE_FAMILIES + FIRST_FAMILY + AndroidConstants.DEFAULT_EXTENSION);
                                    if (familyIcon.exists()) {
                                        int from = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE).getInt(SpecificConstants.OFFLINE_FAMILY_KEY, -1);
                                        synchronizeFamily(from + 1);
                                    } else {
                                        synchronizeFamily(0);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getMessage());
                                    broadcastDownload(-1, -1);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, databaseError.getMessage());
                            broadcastDownload(-1, -1);
                        }
                    });
                }
            });
        }
    }

    private void synchronizePlant(final Integer from, final Integer countAll) {
        DatabaseReference mFirebaseRefPlant = database.getReference(AndroidConstants.FIREBASE_PLANTS_TO_UPDATE
                + AndroidConstants.SEPARATOR + AndroidConstants.FIREBASE_DATA_LIST + AndroidConstants.SEPARATOR + from);
        mFirebaseRefPlant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String plantName = dataSnapshot.getValue(String.class);

                    DatabaseReference mFirebaseRefPlant = database.getReference(AndroidConstants.FIREBASE_PLANTS + AndroidConstants.SEPARATOR + plantName);
                    mFirebaseRefPlant.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                final FirebasePlant plant = dataSnapshot.getValue(FirebasePlant.class);
                                if (plant != null) {

                                    final Query queryHeader = database.getReference().child(AndroidConstants.FIREBASE_PLANTS_HEADERS)
                                            .orderByChild(AndroidConstants.FIREBASE_NAME).equalTo(plant.getName());
                                    queryHeader.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Object listOrMap = dataSnapshot.getValue();
                                            if (listOrMap != null) {
                                                if (listOrMap instanceof ArrayList) {
                                                    GenericTypeIndicator<ArrayList<PlantHeader>> t = new GenericTypeIndicator<ArrayList<PlantHeader>>() {};
                                                    ArrayList<PlantHeader> plantHeaders = dataSnapshot.getValue(t);
                                                    int i = 0;
                                                    while(plantHeaders.get(i) == null) {
                                                        i++;
                                                    }
                                                    if (i+1 == plantHeaders.size()) {
                                                        downloadPlant(from, plantHeaders.get(i), plant, countAll);
                                                    } else {
                                                        broadcastDownload(-1, -1);
                                                    }
                                                } else {
                                                    GenericTypeIndicator<HashMap<String, PlantHeader>> t = new GenericTypeIndicator<HashMap<String, PlantHeader>>() {};
                                                    HashMap<String, PlantHeader> plantHeaders = dataSnapshot.getValue(t);
                                                    if (plantHeaders.values().size() == 1) {
                                                        downloadPlant(from, plantHeaders.values().iterator().next(), plant, countAll);
                                                    } else {
                                                        broadcastDownload(-1, -1);
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.e(TAG, databaseError.getMessage());
                                            broadcastDownload(-1, -1);
                                        }
                                    });
                                } else {
                                    broadcastDownload(-1, -1);
                                }
                            } else {
                                broadcastDownload(-1, -1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, databaseError.getMessage());
                            broadcastDownload(-1, -1);
                        }
                    });
                } else {
                    broadcastDownload(-1, -1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
                broadcastDownload(-1, -1);
            }
        });
    }

    private void downloadPlant(final int number, PlantHeader plantHeader, FirebasePlant firebasePlant, final Integer countAll) {
        FirebaseStorage storage = FirebaseStorage.getInstance(SpecificConstants.STORAGE);

        String localPath = getApplicationContext().getFilesDir() + AndroidConstants.SEPARATOR + AndroidConstants.STORAGE_PHOTOS;
        final int numberOfFiles = 2 + firebasePlant.getPhotoUrls().size() * 2;

        File plantDir = new File(localPath + firebasePlant.getIllustrationUrl().substring(0, firebasePlant.getIllustrationUrl().lastIndexOf('/')));
        if (plantDir.exists()) {
            sk.ab.common.util.Utils.deleteRecursive(plantDir);
        }
        File thumbDir = new File(plantDir.getPath() + AndroidConstants.SEPARATOR + AndroidConstants.THUMBNAIL_DIR);
        thumbDir.mkdirs();

        final SynchronizedCounter counter = new SynchronizedCounter();

        File illustrationFile = new File(localPath + firebasePlant.getIllustrationUrl());
        StorageReference storageRefIllustration = storage.getReference(SpecificConstants.PHOTOS + AndroidConstants.SEPARATOR + firebasePlant.getIllustrationUrl());
        storageRefIllustration.getFile(illustrationFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                counter.increment();
                if (counter.value() == numberOfFiles) {
                    savePlantOffline(number, countAll);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                broadcastDownload(-1, -1);
            }
        });

        if (!plantHeader.getUrl().equals(firebasePlant.getPhotoUrls().get(0))) {
            File photoFile = new File(localPath + plantHeader.getUrl());
            StorageReference storageRefPhoto = storage.getReference(SpecificConstants.PHOTOS + AndroidConstants.SEPARATOR + plantHeader.getUrl());
            storageRefPhoto.getFile(photoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    counter.increment();
                    if (counter.value() == numberOfFiles) {
                        savePlantOffline(number, countAll);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    broadcastDownload(-1, -1);
                }
            });
        } else {
            counter.increment();
            if (counter.value() == numberOfFiles) {
                savePlantOffline(number, countAll);
            }
        }

        for (String photoPath : firebasePlant.getPhotoUrls()) {
            File photoFile = new File(localPath + photoPath);
            StorageReference storageRefPhoto = storage.getReference(SpecificConstants.PHOTOS + AndroidConstants.SEPARATOR + photoPath);
            storageRefPhoto.getFile(photoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    counter.increment();
                    if (counter.value() == numberOfFiles) {
                        savePlantOffline(number, countAll);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    broadcastDownload(-1, -1);
                }
            });

            String thumbPath = Utils.getThumbnailUrl(photoPath);
            File thumbFile = new File(localPath + thumbPath);
            StorageReference storageRefThumb = storage.getReference(SpecificConstants.PHOTOS + AndroidConstants.SEPARATOR + thumbPath);
            storageRefThumb.getFile(thumbFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    counter.increment();
                    if (counter.value() == numberOfFiles) {
                        savePlantOffline(number, countAll);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    broadcastDownload(-1, -1);
                }
            });
        }
    }

    private void savePlantOffline(Integer number, Integer countAll) {
        SharedPreferences preferences = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE);
        int from = preferences.getInt(SpecificConstants.OFFLINE_PLANT_KEY, -1);
        if (from < number) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(SpecificConstants.OFFLINE_PLANT_KEY, number);
            editor.apply();

            broadcastDownload(number, countAll);

            boolean offlineMode = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE).getBoolean(SpecificConstants.OFFLINE_MODE_KEY, false);
            if (offlineMode && BaseApp.isConnectedToWifi(getApplicationContext())) {
                synchronizePlant(number + 1, countAll);
            } else {
                broadcastDownload(-1, -1);
            }
        }
    }

    private void broadcastDownload(Integer number, Integer countAll) {
        Intent localIntent = new Intent(AndroidConstants.BROADCAST_DOWNLOAD)
                .putExtra(AndroidConstants.EXTENDED_DATA_COUNT_ALL, countAll)
                .putExtra(AndroidConstants.EXTENDED_DATA_COUNT_SYNCHONIZED, number);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void synchronizeFamily(final Integer from) {
        DatabaseReference mFirebaseRefPlant = database.getReference(AndroidConstants.FIREBASE_FAMILIES_TO_UPDATE
                + AndroidConstants.SEPARATOR + AndroidConstants.FIREBASE_DATA_LIST + AndroidConstants.SEPARATOR + from);
        mFirebaseRefPlant.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String familyName = dataSnapshot.getValue(String.class);
                    downloadFamily(from, familyName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.getMessage());
            }
        });
    }

    private void downloadFamily(final Integer number, String familyName) {
        FirebaseStorage storage = FirebaseStorage.getInstance(SpecificConstants.STORAGE);

        String localPath = getApplicationContext().getFilesDir() + AndroidConstants.SEPARATOR + AndroidConstants.STORAGE_FAMILIES;

        File plantDir = new File(localPath);
        plantDir.mkdirs();

        String fileName = familyName + AndroidConstants.DEFAULT_EXTENSION;
        File illustrationFile = new File(localPath + fileName);
        StorageReference storageRefIcon = storage.getReference(SpecificConstants.FAMILIES + AndroidConstants.SEPARATOR + fileName);
        storageRefIcon.getFile(illustrationFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                saveFamilyOffline(number);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                broadcastDownload(-1, -1);
            }
        });
    }

    private void saveFamilyOffline(Integer number) {
        SharedPreferences preferences = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE);
        int from = preferences.getInt(SpecificConstants.OFFLINE_FAMILY_KEY, -1);
        if (from < number) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(SpecificConstants.OFFLINE_FAMILY_KEY, number);
            editor.apply();

            boolean offlineMode = getSharedPreferences(SpecificConstants.PACKAGE, Context.MODE_PRIVATE).getBoolean(SpecificConstants.OFFLINE_MODE_KEY, false);
            if (offlineMode && BaseApp.isConnectedToWifi(getApplicationContext())) {
                synchronizeFamily(number + 1);
            }
        }
    }
}
