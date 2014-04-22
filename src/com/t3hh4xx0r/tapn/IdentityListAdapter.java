package com.t3hh4xx0r.tapn;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.t3hh4xx0r.tapn.activities.MainActivity;
import com.t3hh4xx0r.tapn.models.Identity;

public class IdentityListAdapter extends BaseAdapter {
	ArrayList<Identity> list;
	LayoutInflater inf;
	MainActivity act;
	int shownPosition = -1;
	Identity decrypted;

	public IdentityListAdapter(ArrayList<Identity> list, MainActivity act) {
		this.list = list;
		inf = LayoutInflater.from(act);
		this.act = act;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Identity getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	@Override
	public View getView(final int position, View arg1, ViewGroup arg2) {
		View root = inf.inflate(R.layout.identity_card, arg2, false);


		TextView user, pass, name;

		name = (TextView) root.findViewById(R.id.name);
		user = (TextView) root.findViewById(R.id.user);
		pass = (TextView) root.findViewById(R.id.pass);

		final Identity id = getItem(position);
		name.setText(id.getName());

		if (decrypted != null && shownPosition == position) {
			root.findViewById(R.id.details).setVisibility(View.VISIBLE);

			if (id.getUser() != null && !id.getUser().trim().isEmpty()) {
				user.setText("User - " + decrypted.getUser());
			}

			if (id.getPass() != null && !id.getPass().trim().isEmpty()) {
				pass.setText("Password - " + decrypted.getPass());
			}
		} else {
			root.findViewById(R.id.details).setVisibility(View.GONE);
		}

		ImageView view = (ImageView) root.findViewById(R.id.view_icon);
		if (decrypted != null && shownPosition == position) {
			view.setImageResource(R.drawable.ic_dont_view);
		} else {
			view.setImageResource(R.drawable.ic_view);
		}
		root.findViewById(R.id.view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (shownPosition == -1) {
					act.setItemForDecrypt(act.WAITING_FOR_DECRYPT, id, position);
				} else {
					act.removeItemForDecrypt();
				}
			}
		});
		
		root.findViewById(R.id.write).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						act.onItemClick(position);
					}
				});
		return root;
	}

	public void update(ArrayList<Identity> list2) {
		list = list2;
		notifyDataSetChanged();
	}

	public void showIdentity(Identity decrypted, int pos) {
		shownPosition = pos;
		this.decrypted = decrypted;
		notifyDataSetChanged();
	}

	public void remove(Identity item) {
		list.remove(item);		
	}

}
