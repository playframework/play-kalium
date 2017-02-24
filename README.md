# play-scala-secure-session-example

This is an example application that shows how to do simple secure session management in Play, using the Scala API and session cookies.

## Overview

Play has a simple session cookie that is signed, but not encrypted.  This example shows how to securely store information in a client side cookie without revealing it to the browser, by encrypting the data with libsodium, a high level encryption library.

The only server side state is a mapping of session ids to secret keys.  When the user logs out, the mapping is deleted, and the encrypted information cannot be retrieved using the client's session id.  This prevents replay attacks after logout, even if the user saves off the cookies and replays them with exactly the same browser and IP address.

## Prerequisites

As with all Play projects, you must have JDK 1.8 and [sbt](http://www.scala-sbt.org/) installed.

However, you must install libsodium before using this application, which is a non-Java binary install.

If you are on MacOS, you can use Homebrew:

```
brew install libsodium
```

If you are on Ubuntu >= 15.04 or Debian >= 8, you can install with apt-get:

```
apt-get install libsodium-dev
```

On Fedora:

```
dnf install libsodium-devel
```

On CentOS:

```
yum install libsodium-devel
```

For Windows, you can download pre-built libraries using the [install page](https://download.libsodium.org/doc/installation/).

## Running

Run sbt from the command line:

```
sbt run
```

Then go to http://localhost:9000 to see the server.

## Encryption

Encryption is handled by `services.encryption.EncryptionService`.  It uses secret key authenticated encryption with [Kalium](https://github.com/abstractj/kalium/), a thin Java wrapper around libsodium.  Kalium's `SecretBox` is an object oriented mapping to libsodium's `crypto_secretbox_easy` and `crypto_secretbox_open_easy`, described [here](https://download.libsodium.org/doc/secret-key_cryptography/authenticated_encryption.html).  The underlying stream cipher is XSalsa20, used with a Poly1305 MAC.

A abstract [cookie baker](https://www.playframework.com/documentation/latest/api/scala/index.html#play.api.mvc.CookieBaker), `EncryptedCookieBaker` is used to serialize and deserialize encrypted text between a `Map[String, String]` and a case class representation.  `EncryptedCookieBaker` also extends the `JWTCookieDataCodec` trait, which handles the encoding between `Map[String, String` and the raw string data written out in the HTTP response in [JWT format](https://tools.ietf.org/html/rfc7519).

A factory `UserInfoCookieBakerFactory` creates a `UserInfoCookieBaker` that uses the session specific secret key to map a `UserInfo` case class to and from a cookie.

Then finally, a `UserInfoAction`, an action builder, handles the work of reading in a `UserInfo` from a cookie and attaches it to a `UserRequest`, a [wrapped request](https://www.playframework.com/documentation/latest/ScalaActionsComposition) so that the controllers can work with `UserInfo` without involving themselves with the underlying logic.
