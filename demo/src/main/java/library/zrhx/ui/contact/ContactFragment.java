package library.zrhx.ui.contact;

import android.support.v7.widget.RecyclerView;

import com.lib_im.profession.entity.Contact;
import com.lib_im.profession.message.IMUserConversation;
import com.zrhx.base.base.AbstractAutoListFragment;
import com.zrhx.base.base.RequestAction;
import com.zrhx.base.helper.ImageLoader;
import com.zrhx.base.multitype.binder.BaseClickableViewBinder;
import com.zrhx.base.widget.recyclerview.LViewHolder;

import library.zrhx.imsample.R;
import me.drakeet.multitype.MultiTypeAdapter;

public class ContactFragment extends AbstractAutoListFragment<Contact> {

    @Override
    protected void initRecycler(RecyclerView recyclerView, MultiTypeAdapter multiTypeAdapter) {
        multiTypeAdapter.register(Contact.class,
                new BaseClickableViewBinder<Contact>(R.layout.item_contact) {
                    @Override
                    protected void onInitClickableViewHolder(LViewHolder lViewHolder) {

                    }

                    @Override
                    protected void onBindLViewHolder(LViewHolder lViewHolder, Contact contact) {
                        lViewHolder.setText(R.id.contact_name, contact.getNickname());
                        ImageLoader.load(lViewHolder.getView(R.id.contact_head),
                                contact.getHeadicon());
                    }
                });
    }

    @Override
    protected void getData(int i, RequestAction<Contact> requestAction) {
        IMUserConversation.loadContact()
                          .subscribe(requestAction::onRequestSuccess,
                                  throwable -> {});
    }
}
