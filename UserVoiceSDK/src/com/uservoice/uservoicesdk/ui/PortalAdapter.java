package com.uservoice.uservoicesdk.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.UserVoice;
import com.uservoice.uservoicesdk.activity.ContactActivity;
import com.uservoice.uservoicesdk.activity.ForumActivity;
import com.uservoice.uservoicesdk.activity.SearchActivity;
import com.uservoice.uservoicesdk.flow.InitManager;
import com.uservoice.uservoicesdk.model.Article;
import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.model.Forum;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class PortalAdapter extends SearchAdapter<BaseModel> implements AdapterView.OnItemClickListener {

    public static int SCOPE_ALL = 0;
    public static int SCOPE_ARTICLES = 1;
    public static int SCOPE_IDEAS = 2;

    private static int KB_HEADER = 0;
    private static int FORUM = 1;
    private static int TOPIC = 2;
    private static int LOADING = 3;
    private static int CONTACT = 4;
    private static int ARTICLE = 5;
    private static int POWERED_BY = 6;
    private static int CONTACT_HEADER = 7;
    private static int CALL = 8;
    private static int HELP_HEADER = 9;
    private static int HELP_LINK_1 = 10;
    private static int HELP_LINK_2 = 11;
    private static int HELP_LINK_3 = 12;
    private static int HELP_LINK_4 = 13;
    private static int HELP_LINK_5 = 14;

    private static int NUM_VIEW_TYPES = 15;

    private LayoutInflater inflater;
    private final FragmentActivity context;
    private boolean configLoaded = false;
    private List<Integer> staticRows;
    private List<Article> articles;
    private boolean havePhone = false;

    public PortalAdapter(FragmentActivity context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        new InitManager(context, new Runnable() {
            @Override
            public void run() {
                configLoaded = true;
                notifyDataSetChanged();
                loadForum();
                loadTopics();
            }
        }).init();

        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
    }

    private List<Topic> getTopics() {
        return Session.getInstance().getTopics();
    }

    private boolean shouldShowArticles() {
        return Session.getInstance().getConfig(context).getTopicId() != -1 || (getTopics() != null && getTopics().isEmpty());
    }

    private void loadForum() {
        Forum.loadForum(context, Session.getInstance().getConfig(context).getForumId(), new DefaultCallback<Forum>(context) {
            @Override
            public void onModel(Forum model) {
                Session.getInstance().setForum(model);
                notifyDataSetChanged();
            }
        });
    }

    private void loadTopics() {
        final DefaultCallback<List<Article>> articlesCallback = new DefaultCallback<List<Article>>(context) {
            @Override
            public void onModel(List<Article> model) {
                Session.getInstance().setTopics(new ArrayList<Topic>());
                articles = model;
                notifyDataSetChanged();
            }
        };

        if (Session.getInstance().getConfig(context).getTopicId() != -1) {
            Article.loadPageForTopic(context, Session.getInstance().getConfig(context).getTopicId(), 1, articlesCallback);
        } else {
            Topic.loadTopics(context, new DefaultCallback<List<Topic>>(context) {
                @Override
                public void onModel(List<Topic> model) {
                    if (model.isEmpty()) {
                        Session.getInstance().setTopics(model);
                        Article.loadPage(context, 1, articlesCallback);
                    } else {
                        ArrayList<Topic> topics = new ArrayList<Topic>(model);
                        topics.add(Topic.allArticlesTopic(context));
                        Session.getInstance().setTopics(topics);
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void computeStaticRows() {
        if (staticRows == null) {
            staticRows = new ArrayList<Integer>();
            Config config = Session.getInstance().getConfig(context);
            if (config.getHelpLinks().size() > 0) {
                staticRows.add(HELP_HEADER);
                staticRows.add(HELP_LINK_1);
            }
            if (config.getHelpLinks().size() > 1) {
                staticRows.add(HELP_LINK_2);
            }
            if (config.getHelpLinks().size() > 2) {
                staticRows.add(HELP_LINK_3);
            }
            if (config.getHelpLinks().size() > 3) {
                staticRows.add(HELP_LINK_4);
            }
            if (config.getHelpLinks().size() > 4) {
                staticRows.add(HELP_LINK_5);
            }
            if (config.shouldShowContactUs()) {
                staticRows.add(CONTACT_HEADER);
                staticRows.add(CONTACT);
                staticRows.add(CALL);
            } if (config.shouldShowForum())
                staticRows.add(FORUM);
            if (config.shouldShowKnowledgeBase())
                staticRows.add(KB_HEADER);
        }
    }

    @Override
    public int getCount() {
        if (!configLoaded) {
            return 1;
        } else {
            computeStaticRows();
            int rows = staticRows.size();
            if (Session.getInstance().getConfig(context).shouldShowKnowledgeBase()) {
                if (getTopics() == null || (shouldShowArticles() && articles == null)) {
                    rows += 1;
                } else {
                    rows += shouldShowArticles() ? articles.size() : getTopics().size();
                }
            }
            if (!Session.getInstance().getClientConfig().isWhiteLabel()) {
            	rows += 1;
            }
            return rows;
        }
    }

    public List<BaseModel> getScopedSearchResults() {
        if (scope == SCOPE_ALL) {
            return searchResults;
        } else if (scope == SCOPE_ARTICLES) {
            List<BaseModel> articles = new ArrayList<BaseModel>();
            for (BaseModel model : searchResults) {
                if (model instanceof Article)
                    articles.add(model);
            }
            return articles;
        } else if (scope == SCOPE_IDEAS) {
            List<BaseModel> ideas = new ArrayList<BaseModel>();
            for (BaseModel model : searchResults) {
                if (model instanceof Suggestion)
                    ideas.add(model);
            }
            return ideas;
        }
        return null;
    }

    @Override
    public Object getItem(int position) {
        computeStaticRows();
        if (position < staticRows.size() && staticRows.get(position) == FORUM)
            return Session.getInstance().getForum();
        else if (getTopics() != null && !shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < getTopics().size())
            return getTopics().get(position - staticRows.size());
        else if (articles != null && shouldShowArticles() && position >= staticRows.size() && position - staticRows.size() < articles.size())
            return articles.get(position - staticRows.size());
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (!configLoaded)
            return false;
        computeStaticRows();
        if (position < staticRows.size()) {
            int type = staticRows.get(position);
            if (type == KB_HEADER || type == LOADING || type == CONTACT_HEADER)
                return false;
            if (type == CALL && !havePhone)
                return false;
        }
        return true;
    }

    @Override
    protected void searchResultsUpdated() {
        int articleResults = 0;
        int ideaResults = 0;
        for (BaseModel model : searchResults) {
            if (model instanceof Article)
                articleResults += 1;
            else
                ideaResults += 1;
        }
        ((SearchActivity) context).updateScopedSearch(searchResults.size(), articleResults, ideaResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Config config = Session.getInstance().getConfig(context);
        int type = getItemViewType(position);
        if (view == null) {
            if (type == LOADING)
                view = inflater.inflate(R.layout.uv_loading_item, null);
            else if (type == FORUM)
                view = inflater.inflate(R.layout.uv_text_item, null);
            else if (type == KB_HEADER)
                view = inflater.inflate(R.layout.uv_header_item_light, null);
            else if (type == TOPIC)
                view = inflater.inflate(R.layout.uv_text_item, null);
            else if (type == CONTACT)
                view = inflater.inflate(R.layout.uv_text_item, null);
            else if (type == ARTICLE)
                view = inflater.inflate(R.layout.uv_text_item, null);
            else if (type == POWERED_BY)
            	view = inflater.inflate(R.layout.uv_powered_by_item, null);
            else if (type == CONTACT_HEADER)
                view = inflater.inflate(R.layout.uv_header_item_light, null);
            else if (type == CALL)
                view = inflater.inflate(R.layout.uv_text_item, null);
            else if (type == HELP_HEADER)
                view = inflater.inflate(R.layout.uv_header_item_light, null);
            else if (type == HELP_LINK_1 || type == HELP_LINK_2 || type == HELP_LINK_3 || type == HELP_LINK_4 || type == HELP_LINK_5)
                view = inflater.inflate(R.layout.uv_text_item, null);
        }

        if (type == FORUM) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(R.string.uv_feedback_forum);
            TextView text2 = (TextView) view.findViewById(R.id.uv_text2);
            text2.setText(Utils.getQuantityString(text2, R.plurals.uv_ideas, Session.getInstance().getForum().getNumberOfOpenSuggestions()));
        } else if (type == KB_HEADER) {
            TextView textView = (TextView) view.findViewById(R.id.uv_header_text);
            textView.setText(R.string.uv_knowledge_base);
        } else if (type == TOPIC) {
            Topic topic = (Topic) getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(topic.getName());
            textView = (TextView) view.findViewById(R.id.uv_text2);
            if (topic.getId() == -1) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(String.format("%d %s", topic.getNumberOfArticles(), context.getResources().getQuantityString(R.plurals.uv_articles, topic.getNumberOfArticles())));
            }
        } else if (type == CONTACT) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(R.string.uv_write_us);
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == ARTICLE) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            Article article = (Article) getItem(position);
            textView.setText(article.getTitle());
        } else if (type == POWERED_BY) {
        	TextView textView = (TextView) view.findViewById(R.id.uv_version);
        	textView.setText(context.getString(R.string.uv_android_sdk) + " v" + UserVoice.getVersion());
        } else if (type == CONTACT_HEADER) {
            TextView textView = (TextView) view.findViewById(R.id.uv_header_text);
            textView.setText(R.string.uv_contact_us);
        } else if (type == CALL) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(R.string.uv_contact_phone);
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == HELP_HEADER) {
            TextView textView = (TextView) view.findViewById(R.id.uv_header_text);
            textView.setText(R.string.uv_help_header);
        } else if (type == HELP_LINK_1) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(config.getHelpLinks().get(0));
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == HELP_LINK_2) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(config.getHelpLinks().get(1));
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == HELP_LINK_3) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(config.getHelpLinks().get(2));
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == HELP_LINK_4) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(config.getHelpLinks().get(3));
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        } else if (type == HELP_LINK_5) {
            TextView textView = (TextView) view.findViewById(R.id.uv_text);
            textView.setText(config.getHelpLinks().get(4));
            view.findViewById(R.id.uv_text2).setVisibility(View.GONE);
        }

        View divider = view.findViewById(R.id.uv_divider);
        if (divider != null)
            divider.setVisibility((position == getCount() - 2 && getItemViewType(getCount() - 1) == POWERED_BY) || position == getCount() - 1 ? View.GONE : View.VISIBLE);
        if (type == FORUM)
            divider.setVisibility(View.GONE);

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return NUM_VIEW_TYPES;
    }

    @Override
    public int getItemViewType(int position) {
        if (!configLoaded)
            return LOADING;
        computeStaticRows();
        if (position < staticRows.size()) {
            int type = staticRows.get(position);
            if (type == FORUM && Session.getInstance().getForum() == null)
                return LOADING;
            return type;
        }
        if (Session.getInstance().getConfig(context).shouldShowKnowledgeBase()) {
	        if (getTopics() == null || (shouldShowArticles() && articles == null)) {
	        	if (position - staticRows.size() == 0)
	        		return LOADING;
	        } else if (shouldShowArticles() && position - staticRows.size() < articles.size()) {
	        	return ARTICLE;
	        } else if (!shouldShowArticles() && position - staticRows.size() < getTopics().size()) {
	        	return TOPIC;
	        }
        }
        return POWERED_BY;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int type = getItemViewType(position);
        UserVoice.IHelpLinkClickedCallback helpCallback = Session.getInstance().getHelpLinkCallback();
        if (type == CONTACT) {
            context.startActivity(new Intent(context, ContactActivity.class));
        } else if (type == FORUM) {
            context.startActivity(new Intent(context, ForumActivity.class));
        } else if (type == TOPIC || type == ARTICLE) {
            Utils.showModel(context, (BaseModel) getItem(position));
        } else if (type == CALL && havePhone) {
            final Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(context.getString(R.string.uv_contact_phone_uri)));
            context.startActivity(intent);
        } else if (type == HELP_LINK_1 && helpCallback != null) {
            helpCallback.onHelpLinkClicked(context, 0);
        } else if (type == HELP_LINK_2 && helpCallback != null) {
            helpCallback.onHelpLinkClicked(context, 1);
        } else if (type == HELP_LINK_3 && helpCallback != null) {
            helpCallback.onHelpLinkClicked(context, 2);
        } else if (type == HELP_LINK_4 && helpCallback != null) {
            helpCallback.onHelpLinkClicked(context, 3);
        } else if (type == HELP_LINK_5 && helpCallback != null) {
            helpCallback.onHelpLinkClicked(context, 4);
        }
    }

    private final PhoneStateListener phoneListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            havePhone = serviceState.getState() == ServiceState.STATE_IN_SERVICE;
        }
    };
}
