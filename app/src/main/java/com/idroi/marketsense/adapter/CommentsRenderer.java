package com.idroi.marketsense.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idroi.marketsense.CommentTextView;
import com.idroi.marketsense.Logging.MSLog;
import com.idroi.marketsense.R;
import com.idroi.marketsense.common.FrescoImageHelper;
import com.idroi.marketsense.common.MarketSenseRendererHelper;
import com.idroi.marketsense.data.Comment;
import com.idroi.marketsense.data.News;

import java.util.WeakHashMap;

/**
 * Created by daniel.hsieh on 2018/5/9.
 */

public class CommentsRenderer implements MarketSenseRenderer<Comment> {

    @NonNull private final WeakHashMap<View, CommentViewHolder> mViewHolderMap;
    @Nullable private final CommentsRecyclerViewAdapter.OnItemClickListener mOnItemClickListener;
    private final CommentsRecyclerViewAdapter.OnNewsItemClickListener mOnNewsItemClickListener;
    private boolean mIsLargeBorder;

    CommentsRenderer(boolean isLargeBorder,
                     @Nullable CommentsRecyclerViewAdapter.OnItemClickListener listener,
                    CommentsRecyclerViewAdapter.OnNewsItemClickListener newsListener) {
        mViewHolderMap = new WeakHashMap<>();
        mOnItemClickListener = listener;
        mOnNewsItemClickListener = newsListener;
        mIsLargeBorder = isLargeBorder;
    }

    @Override
    public View createView(@NonNull Context context, @Nullable ViewGroup parent) {
        if(mIsLargeBorder) {
            return LayoutInflater
                    .from(context)
                    .inflate(R.layout.comment_list_item_large_border, parent, false);
        } else {
            return LayoutInflater
                    .from(context)
                    .inflate(R.layout.comment_list_item, parent, false);
        }
    }

    @Override
    public void renderView(@NonNull View view, @NonNull final Comment content) {
        CommentViewHolder temp = mViewHolderMap.get(view);
        if(temp == null) {
            temp = CommentViewHolder.convertToViewHolder(view);
            mViewHolderMap.put(view, temp);
        }
        final CommentViewHolder commentViewHolder = temp;

        MarketSenseRendererHelper.addTextView(commentViewHolder.userNameView, content.getUserName());
        MarketSenseRendererHelper.addTextView(commentViewHolder.createTimeView, content.getDateString());
        if(content.getAvatarUrl() != null && !content.getAvatarUrl().isEmpty()) {
            FrescoImageHelper.loadImageView(content.getAvatarUrl(),
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        } else {
            FrescoImageHelper.loadImageView("http://monster.infohubapp.com/monsters/ic_mon_01_y.png",
                    commentViewHolder.avatarView, FrescoImageHelper.ICON_IMAGE_RATIO);
        }

        MarketSenseRendererHelper.addHtmlToTextView(commentViewHolder.commentBodyView, content.getCommentHtml());

        setLikeAndReplyBlock(commentViewHolder, content);

        if(mIsLargeBorder) {
            News news = content.getNews();
            if(news != null) {
                setNewsBlock(commentViewHolder, content.getNews());
            } else {
                if(commentViewHolder.newsBlock != null) {
                    commentViewHolder.newsBlock.setVisibility(View.GONE);
                }
            }
        }

        setViewVisibility(commentViewHolder, View.VISIBLE);
    }

    public void updateLikeAndReplyBlock(View view, Comment comment) {
        CommentViewHolder commentViewHolder = mViewHolderMap.get(view);
        if(commentViewHolder != null) {
            setLikeAndReplyBlock(commentViewHolder, comment);
        }
    }

    private void setLikeAndReplyBlock(CommentViewHolder commentViewHolder, Comment content) {
        int likeNum = content.getLikeNumber();
        int replyNum = content.getReplyNumber();

        commentViewHolder.replyView.setText(String.valueOf(replyNum));

        commentViewHolder.likeView.setText(String.valueOf(likeNum));
        if(content.isLiked()) {
            commentViewHolder.likeBlock.setOnClickListener(null);
            commentViewHolder.likeImageView.setImageResource(R.mipmap.ic_like_on);
        } else {
            commentViewHolder.likeImageView.setImageResource(R.mipmap.ic_like_off);
        }
    }

    private void setNewsBlock(CommentViewHolder commentViewHolder, final News news) {
        if(commentViewHolder.newsBlock != null) {
            commentViewHolder.newsBlock.setVisibility(View.VISIBLE);
            commentViewHolder.newsBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNewsItemClickListener.onNewsItemClick(news);
                }
            });
        }
        MarketSenseRendererHelper.addTextView(commentViewHolder.newsTitleView, news.getTitle());
        MarketSenseRendererHelper.addTextView(commentViewHolder.newsDateView, news.getDate());

        if(commentViewHolder.newsPredictionTextView != null) {
            if (news.isOptimistic()) {
                commentViewHolder.newsPredictionTextView.setBackground(commentViewHolder.newsPredictionTextView.getContext().getResources().getDrawable(R.drawable.btn_oval_small_corner_red));
                commentViewHolder.newsPredictionTextView.setVisibility(View.VISIBLE);
            } else if (news.isPessimistic()) {
                commentViewHolder.newsPredictionTextView.setBackground(commentViewHolder.newsPredictionTextView.getContext().getResources().getDrawable(R.drawable.btn_oval_small_corner_green));
                commentViewHolder.newsPredictionTextView.setVisibility(View.VISIBLE);
            } else {
                commentViewHolder.newsPredictionTextView.setVisibility(View.GONE);
            }
        }

        switch (news.getLevel()) {
            case 3:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_good3);
                break;
            case 2:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_good2);
                break;
            case 1:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_good1);
                break;
            case -1:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_bad1);
                break;
            case -2:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_bad2);
                break;
            case -3:
                commentViewHolder.newsPredictionTextView.setText(R.string.title_news_bad3);
                break;
        }

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) commentViewHolder.horizontalLineView.getLayoutParams();
        params.topToBottom = commentViewHolder.newsBlock.getId();
        commentViewHolder.horizontalLineView.setLayoutParams(params);
    }

    public void setClickListener(View view, final Comment comment, final int position) {
        final CommentViewHolder commentViewHolder = mViewHolderMap.get(view);
        if(commentViewHolder != null) {
            commentViewHolder.replyBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onReplyItemClick(comment, position);
                    }
                }
            });

            if (!comment.isLiked()) {
                commentViewHolder.likeBlock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onSayLikeItemClick(comment, position);
                        }
                    }
                });
            }

            commentViewHolder.commentBodyView.setOnReachMaxHeightListener(new CommentTextView.OnReachMaxHeightListener() {
                @Override
                public void onReachMaxHeight() {
                    setReadMoreTextView(commentViewHolder, true, comment, position);
                }
            });
            setReadMoreTextView(commentViewHolder, false, comment, position);
        } else {
            MSLog.e("commentViewHolder should not be null in setClickListener.");
        }
    }

    private void setReadMoreTextView(final CommentViewHolder commentViewHolder, final boolean show, final Comment comment, final int position) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) commentViewHolder.horizontalLineView.getLayoutParams();
                if(show) {
                    if(comment.getNews() != null && commentViewHolder.newsBlock != null) {
                        params.topToBottom = commentViewHolder.newsBlock.getId();
                    } else {
                        params.topToBottom = commentViewHolder.readMoreView.getId();
                    }
                    commentViewHolder.readMoreView.setVisibility(View.VISIBLE);
                    commentViewHolder.readMoreView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(mOnItemClickListener != null) {
                                mOnItemClickListener.onReplyItemClick(comment, position);
                            }
                        }
                    });
                } else {
                    if(comment.getNews() != null && commentViewHolder.newsBlock != null) {
                        params.topToBottom = commentViewHolder.newsBlock.getId();
                    } else {
                        params.topToBottom = commentViewHolder.commentBodyView.getId();
                    }
                    commentViewHolder.readMoreView.setVisibility(View.GONE);
                    commentViewHolder.readMoreView.setOnClickListener(null);
                }
                commentViewHolder.horizontalLineView.setLayoutParams(params);
            }
        });
    }

    private void setViewVisibility(final CommentViewHolder commentViewHolder, final int visibility) {
        if(commentViewHolder.mainView != null) {
            commentViewHolder.mainView.setVisibility(visibility);
        }
    }

    @Override
    public void clear() {
        mViewHolderMap.clear();
    }
}
