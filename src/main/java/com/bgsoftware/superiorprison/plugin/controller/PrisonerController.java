package com.bgsoftware.superiorprison.plugin.controller;

import com.bgsoftware.superiorprison.api.controller.IPrisonerController;
import com.oop.orangeengine.database.ODatabase;
import com.oop.orangeengine.database.object.DataController;

public class PrisonerController extends DataController implements IPrisonerController {

    public PrisonerController(ODatabase database) {
        super(database);
    }
}
