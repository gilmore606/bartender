# BarBack

Public source code for the BarBack android cocktail-recipe reference app.

## What's this?

This is a free Android app for cataloging one's home bar and discovering drink recipes that can be made from it.  There are many similar
apps on the Play store, but I made this one for my own (and my wife's) use.

This app was primarily made as a portfolio work example and took about two weeks start to finish.

## Can I try it?

Sure!  It's available for free (and ad-free) on the Play store [here](https://play.google.com/store/apps/details?id=com.dlfsystems.bartender).

## How is it made?

It's written in Kotlin and makes use of these libraries:

- Retrofit
- RxJava/RxAndroid
- SimpleStack - an extension of the popular Flow library for fragment backstack management
- PagedList
- ViewModel
- LiveData
- Room

The architecture is roughly MVI inspired and uses immutable states and unidirectional data flow.  Everything is backed by the Room db
and accessed through ViewModel and LiveData.  Rx is used in the underlying architecture for piping intents and states around.
