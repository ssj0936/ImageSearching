# Image Searching

A simple app for user to search images from webapi(pixabay) and providing 2 type of displaymode which are grip and list.
Implementation of MVVM architecture using Hilt, Paging, RxJava and Retrofit2

The app has following packages:

1. api: repository and datasource interface
    - local : local datasource(sharedPreference)
    - network : webAPI(retrofit2)
    - model : data model(pojo)
2. di: Module of Hilt
3. ui: Fragments / viewmodels / recyclerview adapters
4. utils: Utility classes

### Several details.
1. For solving **IOException java.io.IOException: Cleartext HTTP traffic to * not permitted**, adding a additional network secure config: **<base-config cleartextTrafficPermitted="true" />** is required.
2. For switching searching result layout dynamically with one recyclerView adapter, implementing 2 type of item layout and override **getItemViewType** method for adapter is required. **setLayoutType** method in adapter is a flag switching method for notifying adapter current layout type.
3. Passing livedata **loadStatus** in viewmodel to paging datasource for handleing loading status. Observing changing of **loadStatus** statuse in fragment to manipulate wigets like loading progressbar, snackbar message, and retry button
4. Implementation of history recording by **sharepreference**. **searchTermsHistory** is a MediatorLiveData for showing history terms list and also updating value when user inputing a new search terms, it initials when viewmodel creating and save to sharepreference after updateing its value.
