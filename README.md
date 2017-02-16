# web-push

Scala library for easy Web Push message sending. This library uses latest VAPID identification protocol. **Supports Chrome 52+ and Firefox 46+**

# Install

Using SBT.

    libraryDependencies += "com.zivver" %% "web-push" % "0.1.1"

# Basic Usage

The common use case for this library is an application server using
a GCM API key and VAPID keys.

```scala
// Base64 string server public/private key
  private val vapidPublicKey: PublicKey = Utils.loadPublicKey("...server public key...")
  private val vapidPrivateKey: PrivateKey = Utils.loadPrivateKey(...server private key...)

// Initialize pushService with VAPID keys and subscriber (mailto or your application domain)
val pushService = PushService(vapidPublicKey, vapidPrivateKey, "mailto:your-app@example.com")

// Create a Subscription from browser subscription data
val subscription = Subscription("endpoint", "p256dh", "auth")

// Send a non data notification
pushService.send(subscription)

// Send a data bearing notification
pushService.send(subscription, "Hi there!")

```

## Generating VAPID keys

There are several ways to generate VAPID keys. Easiest way to do this is to use an existing library

 * Using the web-push node library CLI
 * Using OpenSSL
 * Any of the libraries in  libraries in https://github.com/web-push-libs/vapid
 
### Using the web-push node library CLI

```javascript
npm install web-push -g
web-push generate-vapid-keys [--json]
```
Will output a json with the key pair url safe base64 encoded.
```json
{"publicKey":"BDrsEIWlTy1YTAZxpkN1f1C0EcuCjL15j8lxS3KaXzDE_BvlWIHEIGdmsP3hfiiG3ldbF89pWEc6foyFxSOe5es","privateKey":"lDLZKT9oZF07KJYWBZU2zlHfszrK4p9tFtxM-ihpVqs"}
```
This strings can be used directly into
```scala
Utils.loadPublicKey("...server public key String...")
Utils.loadPrivateKey("...server private key String...")
```
### Using OpenSSL
OpenSSL can create 2 .pem files containing the public and private keys.
```
openssl ecparam -name prime256v1 -genkey -noout -out vapid_private.pem
openssl ec -in vapid_private.pem -pubout -out vapid_public.pem
```
Read this files and strip the “-----BEGIN PUBLIC KEY------” and “-----END PUBLIC KEY-----” lines, remove the newline characters, and convert all “+” to “-” and “/” to “_”.

## Client side notes

When subscribing the client needs to send the application server key as a Unit8Array

```javascript
if ('showNotification' in ServiceWorkerRegistration.prototype) {
  navigator.serviceworker.ready
  .then(registration => {
    return registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: new Uint8Array([...])
    });
  })
  .then(subscription => {
    // Send subscription to your application server.
  })
  .catch(error => {
    // Do something with the error.
  });
}
```

To transform the base64 VAPID public key to a Unit8Array

```javascript
function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4);
  const base64 = (base64String + padding)
    .replace(/\-/g, '+')
    .replace(/_/g, '/');

  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);

  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

const vapidPublicKey = 'Your server base64 url safe public key';
const convertedVapidKey = urlBase64ToUint8Array(vapidPublicKey);

registration.pushManager.subscribe({
  userVisibleOnly: true,
  applicationServerKey: convertedVapidKey
});
```

# Help

* Google developers [Web Push Notifications](https://developers.google.com/web/fundamentals/engage-and-retain/push-notifications/)

* Mozilla developer network [Using Push API](https://developer.mozilla.org/en-US/docs/Web/API/Push_API/Using_the_Push_API)

# Credit
This library is mostly a Scala port from [MartijnDwars/web-push](https://github.com/MartijnDwars/web-push) and [web-push-libs/web-push](web-push-libs/web-push)
