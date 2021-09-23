# Image Searching
<p>
<img src="https://user-images.githubusercontent.com/3841546/134450008-927215bb-c80e-49a0-a93a-3df87116f4d5.png" width="270" height="612">
<img src="https://user-images.githubusercontent.com/3841546/134450004-40f40890-f07b-4f7b-9e01-cbcc3880d85b.png" width="270" height="612">
<img src="https://user-images.githubusercontent.com/3841546/134450007-98d3be92-65c9-496c-8fa9-51bb7b07bb9e.png" width="270" height="612">
</p>

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
1. For solving **IOException java.io.IOException: Cleartext HTTP traffic to * not permitted**, adding a additional network secure config: **<base-config cleartextTrafficPermitted="true" \/>** is required.
2. For switching searching result layout dynamically with one recyclerView adapter, implementing 2 type of item layout and override **getItemViewType** method for adapter is required. **setLayoutType** method in adapter is a flag switching method for notifying adapter current layout type.
3. Passing livedata **loadStatus** in viewmodel to paging datasource for handleing loading status. Observing changing of **loadStatus** statuse in fragment to manipulate wigets like loading progressbar, snackbar message, and retry button.
4. Implementation of history recording by **sharepreference**. **searchTermsHistory** is a MediatorLiveData for showing history terms list and also updating value when user inputing a new search terms, it initials when viewmodel creating and save to sharepreference after updateing its value.
5. Setting **@BindingAdapter** for binding image's url to imageview with Glide make it less redundent boilerplate in code.
6. **Navigation** is just a routine for single Activity app and considering of flexibility need but not necessary currently.
7. Default value like: HISTORY_MAX_SIZE(history display number), IMAGE_SEARCH_PAGE_SIZE(images fetching page size) and DEFAULT_LAYOUT_TYPE(default image list layout) are in Constants.kt

### DEMO apk download
https://www.dropbox.com/s/bdovpj0wcmv9mn9/image_searching.apk?dl=0
