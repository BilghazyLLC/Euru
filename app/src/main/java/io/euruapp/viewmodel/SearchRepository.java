package io.euruapp.viewmodel;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import io.codelabs.recyclerview.BaseDataManager;
import io.euruapp.core.BaseActivity;
import io.euruapp.model.EuruSearchableItem;
import io.euruapp.model.User;
import io.euruapp.util.ConstantsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.euruapp.util.ConstantsUtils.logResult;

public abstract class SearchRepository extends BaseDataManager<List<? extends EuruSearchableItem>> {
	
	private BaseActivity activity;
	private FirebaseFirestore firestore;
	private Map<String, Task<QuerySnapshot>> inflight = new HashMap<String, Task<QuerySnapshot>>(0);
	
	public SearchRepository(@NotNull Context context, BaseActivity activity) {
		super(context);
		this.activity = activity;
		this.firestore = activity.getFirestore();
	}
	
	@Override
	public void cancelLoading() {
		if (!inflight.isEmpty()) inflight.clear();
	}
	
	public void searchFor(String q) {
		loadStarted();
		
		Task<QuerySnapshot> snapshotTask = firestore.collection(ConstantsUtils.COLLECTION_USERS).get();
		inflight.put(q, snapshotTask);
		
		snapshotTask.addOnCompleteListener(activity, new OnCompleteListener<QuerySnapshot>() {
			@Override
			public void onComplete(@NonNull Task<QuerySnapshot> task) {
				if (task.isSuccessful()) {
					QuerySnapshot result = task.getResult();
					if (result != null) {
						List<User> items = result.toObjects(User.class);

						for (DocumentSnapshot snapshot : result) {
							if (snapshot != null && snapshot.exists()) {
								User user = snapshot.toObject(User.class);
								if (user != null) {
									if (user.name != null && !user.name.isEmpty() && user.name.contains(q) || user.type.contains(q))
										items.add(user);
								}
							}
						}
						
						loadFinished();
						onDataLoaded(items);
						inflight.remove(q);
					}
				} else {
					loadFinished();
					onDataLoaded(new ArrayList<>(0));
					inflight.remove(q);
				}
			}
		})
				.addOnFailureListener(activity, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						logResult(e.getLocalizedMessage());
						loadFinished();
						onDataLoaded(new ArrayList<>(0));
						inflight.remove(q);
					}
				});
	}
	
}
