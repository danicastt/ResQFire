# ResQFire - Uputstvo za podešavanje

## Korak 1: Firebase projekat

1. Idi na https://console.firebase.google.com
2. Klikni "Add project" -> ime: ResQFire
3. Uključi Google Analytics (može i bez)
4. U projektu aktiviraj ove servise (levo meni "Build"):
   - **Authentication** -> Sign-in method -> Email/Password -> Enable
   - **Firestore Database** -> Create database -> Start in test mode
   - **Storage** -> Get started -> Start in test mode
   - **Cloud Messaging** (automatski aktivan)

## Korak 2: Dodaj Android aplikaciju u Firebase

1. U Firebase konzoli klikni ikonu Android (</>)
2. Package name: `com.resqfire`
3. App nickname: ResQFire
4. Klikni "Register app"
5. **Skini google-services.json**
6. Zameni fajl `app/google-services.json` sa preuzetim fajlom

## Korak 3: Google Maps API Key

1. Idi na https://console.cloud.google.com
2. Pronađi projekat koji je Firebase kreirao
3. APIs & Services -> Enable APIs -> omogući "Maps SDK for Android"
4. APIs & Services -> Credentials -> Create Credentials -> API Key
5. Kopiraj API key
6. U fajlu `app/src/main/AndroidManifest.xml` zameni:
   `YOUR_MAPS_API_KEY` sa tvojim API key-em

## Korak 4: Otvori projekat

1. Android Studio -> Open -> izaberi folder ResQFire
2. Sačekaj da Gradle sinhronizuje (može potrajati)
3. Run -> Run 'app'

## Korak 5: Firestore pravila (opciono za produkciju)

U Firebase konzoli -> Firestore -> Rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
