package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.IPlayerController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;

public class PlayerController extends DataController implements IPlayerController {

    public PlayerController(ODatabase database) {
        super(database);
    }
}
