package y2k.joyreactor;

import rx.Observable;
import y2k.joyreactor.requests.PostsForTagRequest;

/**
 * Created by y2k on 03/11/15.
 */
public class PostListSynchronizer {

    private Repository<Post> repository;
    private PostMerger merger;
    private PostsForTagRequest.Factory requestFactory;

    private PostsForTagRequest request;

    PostListSynchronizer(Repository<Post> repository,
                         PostsForTagRequest.Factory requestFactory) {
        this.merger = new PostMerger(repository);
        this.repository = repository;
        this.requestFactory = requestFactory;
    }

    public Observable<Boolean> preloadNewPosts() {
        request = getPostsForTagRequest(null);
        return request
                .requestAsync()
                .flatMap(s -> merger.isUnsafeUpdate(request.getPosts()));
    }

    public Observable<Void> applyNew() {
        return merger.mergeFirstPage(request.getPosts());
    }

    public Integer getDivider() {
        return merger.getDivider();
    }

    public Observable<Void> loadNextPage() {
        request = getPostsForTagRequest(request.getNextPageId());
        return request
                .requestAsync()
                .flatMap(s -> merger.mergeNextPage(request.getPosts()));
    }

    public Observable<Void> reloadFirstPage() {
        request = getPostsForTagRequest(null);
        return request
                .requestAsync()
                .flatMap(s -> repository.clearAsync())
                .flatMap(s -> merger.mergeFirstPage(request.getPosts()));
    }

    private PostsForTagRequest getPostsForTagRequest(String pageId) {
        return requestFactory.make(null, pageId);
    }

    public static class Factory {

        private Repository<Post> repository = new Repository<>(Post.class);
        private PostsForTagRequest.Factory requestFactory = new PostsForTagRequest.Factory();

        public PostListSynchronizer make() {
            return new PostListSynchronizer(repository, requestFactory);
        }
    }
}