package org.edx.mobile.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.util.ResourceUtil;

import uk.co.chrisjenx.calligraphy.CalligraphyTypefaceSpan;
import uk.co.chrisjenx.calligraphy.TypefaceUtils;

public class DiscussionPostsAdapter extends BaseListAdapter<DiscussionThread> {
    @ColorInt
    private final int edx_brand_primary_base;
    @ColorInt
    private final int edx_grayscale_neutral_dark;
    @ColorInt
    private final int edx_brand_secondary_dark;
    @ColorInt
    private final int edx_utility_success_dark;

    // Record the current time at initialization to keep the display of the elapsed time durations stable.
    private long initialTimeStampMs = System.currentTimeMillis();

    private final Typeface openSansSemiBoldFont;

    @Inject
    public DiscussionPostsAdapter(Context context, IEdxEnvironment environment) {
        super(context, R.layout.row_discussion_thread, environment);
        edx_brand_primary_base = context.getResources().getColor(R.color.edx_brand_primary_base);
        edx_grayscale_neutral_dark = context.getResources().getColor(R.color.edx_grayscale_neutral_dark);
        edx_brand_secondary_dark = context.getResources().getColor(R.color.edx_brand_secondary_dark);
        edx_utility_success_dark = context.getResources().getColor(R.color.edx_success_text);
        openSansSemiBoldFont = TypefaceUtils.load(context.getAssets(), "fonts/OpenSans-Semibold.ttf");
    }

    @Override
    public void render(BaseViewHolder tag, DiscussionThread discussionThread) {
        ViewHolder holder = (ViewHolder) tag;
        {
            final Icon icon;
            @ColorInt
            final int iconColor;
            if (discussionThread.getType() == DiscussionThread.ThreadType.QUESTION) {
                if (discussionThread.isHasEndorsed()) {
                    icon = FontAwesomeIcons.fa_check_square_o;
                    iconColor = edx_utility_success_dark;
                } else {
                    icon = FontAwesomeIcons.fa_question;
                    iconColor = edx_brand_secondary_dark;
                }
            } else {
                icon = FontAwesomeIcons.fa_comments;
                iconColor = (discussionThread.isRead() ? edx_grayscale_neutral_dark : edx_brand_primary_base);
            }
            holder.discussionPostTypeIcon.setIcon(icon);
            holder.discussionPostTypeIcon.setIconColor(iconColor);
        }

        {
            final SpannableString threadTitle = new SpannableString(discussionThread.getTitle());
            if (!discussionThread.isRead()) {
                CalligraphyTypefaceSpan boldSpan = new CalligraphyTypefaceSpan(openSansSemiBoldFont);
                threadTitle.setSpan(boldSpan, 0, threadTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.discussionPostTitle.setText(threadTitle);
            } else {
                // Calling toString will truncate the spannable properties applied (if any)
                holder.discussionPostTitle.setText(threadTitle.toString());
            }
        }

        holder.discussionPostClosedIcon.setVisibility(discussionThread.isClosed() ? View.VISIBLE : View.GONE);
        holder.discussionPostPinIcon.setVisibility(discussionThread.isPinned() ? View.VISIBLE : View.GONE);
        holder.discussionPostFollowIcon.setVisibility(discussionThread.isFollowing() ? View.VISIBLE : View.GONE);

        {
            final int commentCount = discussionThread.getCommentCount();
            if (commentCount == 0) {
                holder.discussionPostRepliesTextView.setVisibility(View.GONE);
            } else {
                CharSequence totalReplies = ResourceUtil.getFormattedString(
                        getContext().getResources(), R.string.discussion_post_total_replies,
                        "total_replies", commentCount > 99 ? "99+" : String.valueOf(commentCount));
                if (isAnyIconVisible(discussionThread)) {
                    totalReplies = "| " + totalReplies;
                }
                holder.discussionPostRepliesTextView.setText(totalReplies);
                holder.discussionPostRepliesTextView.setVisibility(View.VISIBLE);
            }
        }

        {
            CharSequence lastPostDate = DiscussionTextUtils.getRelativeTimeSpanString(getContext(),
                    initialTimeStampMs, discussionThread.getUpdatedAt().getTime(),
                    DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_SHOW_YEAR);
            if (isAnyIconVisible(discussionThread) || discussionThread.getCommentCount() != 0) {
                lastPostDate = " | " + lastPostDate;
            }
            holder.discussionPostDateTextView.setText(lastPostDate);
        }

        {
            final int unreadCommentCount = discussionThread.getUnreadCommentCount();
            if (unreadCommentCount == 0) {
                holder.discussionUnreadRepliesTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.discussionUnreadRepliesTextView.setVisibility(View.VISIBLE);
                holder.discussionUnreadRepliesTextView.setText(unreadCommentCount > 99 ? "99+" : String.valueOf(unreadCommentCount));
            }
        }
    }

    @Override
    public BaseViewHolder getTag(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
     * @param thread {@link DiscussionThread} object.
     * @return <code>true</code> if a thread is closed, pinned or being followed by a user,
     * <code>false</code> otherwise.
     */
    public boolean isAnyIconVisible(@NonNull DiscussionThread thread) {
        return thread.isClosed() || thread.isFollowing() || thread.isPinned();
    }

    private static class ViewHolder extends BaseViewHolder {
        final IconImageView discussionPostTypeIcon;
        final TextView discussionPostTitle;
        final IconImageView discussionPostClosedIcon;
        final IconImageView discussionPostPinIcon;
        final IconImageView discussionPostFollowIcon;
        final TextView discussionPostRepliesTextView;
        final TextView discussionPostDateTextView;
        final TextView discussionUnreadRepliesTextView;

        public ViewHolder(View convertView) {
            discussionPostTypeIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_type_icon);
            discussionPostTitle = (TextView) convertView.findViewById(R.id.discussion_post_title);
            discussionPostClosedIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_closed_icon);
            discussionPostPinIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_pin_icon);
            discussionPostFollowIcon = (IconImageView) convertView.findViewById(R.id.discussion_post_following_icon);
            discussionPostRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_post_replies_count);
            discussionPostDateTextView = (TextView) convertView.findViewById(R.id.discussion_post_date);
            discussionUnreadRepliesTextView = (TextView) convertView.findViewById(R.id.discussion_unread_replies_text);
        }
    }
}
