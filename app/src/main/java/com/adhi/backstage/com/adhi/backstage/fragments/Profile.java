package com.adhi.backstage.com.adhi.backstage.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adhi.backstage.R;
import com.adhi.backstage.com.adhi.backstage.cardlist.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class Profile extends Fragment {
    private DatabaseReference mDatabase;
    private TextView tName, tEmail, tRole;
    private ImageView iAvatar;
    private FirebaseAuth auth;
    private Button bAvatar, bPhone, bPassword;
    private User cUser;
    FirebaseUser fbUser;
    private ProgressDialog loadingDialog;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        loadingDialog = new ProgressDialog(getContext());
        loadingDialog.setMessage("Loading...");
        loadingDialog.setIndeterminate(true);
        loadingDialog.show();

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(auth.getCurrentUser().getEmail().split("\\@")[0]);
        fbUser = FirebaseAuth.getInstance().getCurrentUser();

        tName = (TextView) rootView.findViewById(R.id.profile_name);
        tEmail = (TextView) rootView.findViewById(R.id.profile_email);
        tRole = (TextView) rootView.findViewById(R.id.profile_role);
        iAvatar = (ImageView) rootView.findViewById(R.id.profile_avatar);
        bAvatar = (Button) rootView.findViewById(R.id.btn_change_avatar);
        bPhone = (Button) rootView.findViewById(R.id.btn_change_phone);
        bPassword = (Button) rootView.findViewById(R.id.btn_change_password);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                cUser = new User(dataSnapshot.getKey().toString(),
                        dataSnapshot.child("name").getValue().toString(),
                        dataSnapshot.child("avatar").getValue().toString(),
                        dataSnapshot.child("role").getValue().toString(),
                        dataSnapshot.child("phone").getValue().toString());
                tName.setText(dataSnapshot.child("name").getValue().toString());
                tEmail.setText(auth.getCurrentUser().getEmail());
                tRole.setText(Members.getRoleByVal(dataSnapshot.child("role").getValue().toString()));
                int avatarResId = R.drawable.avatar_0;
                switch (dataSnapshot.child("avatar").getValue().toString()) {
                    case "0":
                        avatarResId = R.drawable.avatar_0;
                        break;
                    case "1":
                        avatarResId = R.drawable.avatar_1;
                        break;
                    case "2":
                        avatarResId = R.drawable.avatar_2;
                        break;
                    case "3":
                        avatarResId = R.drawable.avatar_3;
                        break;
                    case "4":
                        avatarResId = R.drawable.avatar_4;
                        break;
                    case "5":
                        avatarResId = R.drawable.avatar_5;
                        break;
                    case "6":
                        avatarResId = R.drawable.avatar_6;
                        break;
                    case "7":
                        avatarResId = R.drawable.avatar_7;
                        break;
                    case "8":
                        avatarResId = R.drawable.avatar_8;
                        break;
                    case "9":
                        avatarResId = R.drawable.avatar_9;
                        break;
                    case "10":
                        avatarResId = R.drawable.avatar_10;
                        break;
                }
                iAvatar.setImageResource(avatarResId);
                loadingDialog.cancel();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(postListener);

        bAvatar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HashMap<String, Object> a = new HashMap<>();
                cUser.avatar = String.valueOf((Integer.valueOf(cUser.getAvatar()) + 1) % 11);
                a.put("avatar", Integer.valueOf(cUser.getAvatar()));
                mDatabase.updateChildren(a);
            }
        });

        bPhone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Change Phone Number");
                final EditText input = new EditText(getContext());
                alert.setView(input);
                input.setText(cUser.getPhone());
                alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        HashMap<String, Object> a = new HashMap<>();
                        String curPhone = input.getText().toString();
                        if (curPhone.matches("^[+]?[0-9]{10,13}$") && curPhone.length() >= 10
                                && curPhone.length() <= 13) {
                            a.put("phone", input.getText().toString());
                            Toast.makeText(getContext(), "Phone Number Updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
                        }
                        mDatabase.updateChildren(a);
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                alert.show();
            }
        });

        bPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("Change Password");
                final LinearLayout LL = new LinearLayout(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                LL.setLayoutParams(params);
                LL.setOrientation(LinearLayout.VERTICAL);
                final EditText newPass1 = new EditText(getContext());
                final EditText newPass2 = new EditText(getContext());
                newPass1.setHint("New Password");
                newPass2.setHint("Confirm New Password");
                newPass1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                newPass2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                LL.addView(newPass1, android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                LL.addView(newPass2, android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                alert.setView(LL);
                alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (newPass1.getText().toString().equals(newPass2.getText().toString())) {
                            fbUser.updatePassword(newPass1.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Password is updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to update password!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });

                alert.show();
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
