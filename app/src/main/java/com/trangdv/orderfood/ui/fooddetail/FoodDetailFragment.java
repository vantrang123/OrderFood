package com.trangdv.orderfood.ui.fooddetail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.database.Database;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.ui.FoodActivity;
import com.trangdv.orderfood.viewholder.FoodViewHolder;

import java.text.NumberFormat;
import java.util.Locale;

public class FoodDetailFragment extends Fragment {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    FloatingActionButton btnCart;
    ElegantNumberButton amountButton;
    FirebaseDatabase database;
    DatabaseReference foods;
    Food currentFood;
    Toolbar toolbar;
    AppBarLayout appBarLayout;

    String foodId = "";
    int lastQuantity = 0;

    Database db;

    public static FoodDetailFragment newInstance(String id) {
        FoodDetailFragment foodDetailFragment = new FoodDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("FoodId", id);
        foodDetailFragment.setArguments(bundle);
        return foodDetailFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_detail, container, false);

        initView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        db = new Database(getContext());
        foods = database.getReference("Foods");
        if (getArguments() != null) {
            foodId = getArguments().getString("FoodId");
        }
        if (!foodId.isEmpty()) {
            getDetailFood(foodId);
        }
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Locale locale = new Locale("vi", "VN");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                int price = (Integer.parseInt(currentFood.getPrice()));


                if (currentFood.getBitmapImage()==null) {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(currentFood.getImage())
                            .fitCenter()
                            .centerCrop()
                            .listener(new RequestListener<Bitmap>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                    currentFood.setBitmapImage(resource);
                                    return false;
                                }
                            })
                            .into(food_image);
                } else {
                    food_image.setImageBitmap(currentFood.getBitmapImage());
                }

                food_price.setText(fmt.format(price));
                food_name.setText(currentFood.getName());
                food_description.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initView(View view) {
        appBarLayout = view.findViewById(R.id.appbar_fd);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back, getActivity().getTheme()));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case COLLAPSED:
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        toolbar.setTitle(currentFood.getName());
                        break;
                    case IDLE:
                    case EXPANDED:
                        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                        toolbar.setTitle("");
                        break;
                    default:
                        break;
                }
            }
        });

        amountButton = view.findViewById(R.id.amount);
        btnCart = view.findViewById(R.id.btn_add_cart);


        food_description = view.findViewById(R.id.food_description);
        food_name = view.findViewById(R.id.food_detail_name);
        food_price = view.findViewById(R.id.food_detail_price);
        food_image = view.findViewById(R.id.food_detail_img);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!db.IsProductExist(foodId)) {
                    db.addToCart(new Order(
                            foodId,
                            currentFood.getName(),
                            amountButton.getNumber(),
                            currentFood.getPrice(),
                            currentFood.getDiscount(),
                            currentFood.getImage()
                    ));
                } else {
                    String num = db.getItem(foodId).getQuanlity();
                    String quantity = String.valueOf(Integer.valueOf(num) + Integer.valueOf(amountButton.getNumber()));
//                    String quantity = String.valueOf(lastQuantity + Integer.valueOf(amountButton.getNumber()));
                     db.updateCart(foodId, quantity);
                }

                Toast.makeText(getActivity(), R.string.added_to_cart, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
