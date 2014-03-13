
CarCast responds to the following broadcast intents:

   - com.jadn.cc.services.external.PAUSE
   - com.jadn.cc.services.external.PLAY
   - com.jadn.cc.services.external.PAUSEPLAY
   - com.jadn.cc.services.external.DOWNLOAD

These can be used to control CarCast from outside of the CarCast app
itself.

For example, when testing via adb:

   - adb shell am broadcast '"intent:#Intent;action=com.jadn.cc.services.external.PAUSEPLAY;end"'

Or, more realistically, automation apps such as Tasker can have CarCast
start and stop playback in response to various events (for example,
headset-inserted).  For Tasker, the necessary task is:

   - Misc / Send Intent:
     - Action: one of the four intents, above
     - Cat: None
     - Leave everything else at its default value except, ...
     - Target: Broadcast Receiver

