### general

* private instances (friends only)
* Mastodon integration? expose the feed over Mastodon ActivityPub protocol
* use tags, allow users to subscribe to tags to follow them
* allow users to follow other users
* moderation, instance admin should be able to deputize moderators to help manage content
* the model for the feed could work closer to Twitter/Mastodon than Reddit
* users could subscribe to a combination of tags and other users
* users should be able to create lists from tags/users for customized feeds
* ability to block users
* ability to subscribe to RSS feeds via Krueger, possibly use RSS to share feeds between Krueger instances?
* allow federated user accounts, users from one instance could comment on another

### posts and comments

* allow three types of posts: text/link/media
* allow for media posts, where multiple images/videos can be shared in a single post
* repost detection, if a link has already been posted, take the user there
* content warnings
* markdown in comments
* allow media attachments in comments

### post propagation

Use Twitter style model where users reshare posts instead of upvoting. This will make the post
show up in the timeline of the user resharing the post and become visible to their followers.

Have a view to show posts by number of reshares and the category within a given time period.

### Federated identity

* Each user has a home server with a private and public keys
* user profile is encrypted using the private key
* users can export the public key to other servers
* servers create an account for the user locally with their public key
* when the user logs in to a remote server it will ask the home server for the profile and try to decrypt it using the public key, if the profile decrypts successfully it confirms the identity of the user

### focus

Encourage focus on local news, or small groups with shared interests.

