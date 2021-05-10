/*
 * Copyright or © or Copr. AmauryCarrade (2015)
 * 
 * http://amaury.carrade.eu
 * 
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package eu.carrade.amaury.ballsofzirconium.timers;

import eu.carrade.amaury.ballsofzirconium.BallsOfZirconium;
import fr.zcraft.quartzlib.core.QuartzComponent;
import fr.zcraft.quartzlib.tools.runners.RunTask;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;


public class Timers extends QuartzComponent
{
    private static Set<Timer> timers = new HashSet<>();
    private UpdateTimerTask updateTimerTask;

    @Override
    protected void onEnable()
    {
        RunTask.timer(updateTimerTask = new UpdateTimerTask(), 0l, 20l);
    }

    @Override
    protected void onDisable()
    {
        updateTimerTask.cancel();
        updateTimerTask = null;
    }


    static void registerTimer(final Timer timer)
    {
        timers.add(timer);
        timer.setRegistered(true);
    }

    static void unregisterTimer(final Timer timer)
    {
        timers.remove(timer);
        timer.setRegistered(false);
    }


    private static class UpdateTimerTask extends BukkitRunnable
    {
        @Override
        public void run()
        {
            for (Timer timer : timers)
            {
                timer.update();
            }


            if (BallsOfZirconium.get().getGameManager().isGameRunning())
            {
                if (BallsOfZirconium.get().getBarManager().isEnabled())
                {
                    BallsOfZirconium.get().getBarManager().updatePercentage();
                }
                else
                {
                    BallsOfZirconium.get().getScoreboardManager().updateTimer();
                }
            }
        }
    }
}
