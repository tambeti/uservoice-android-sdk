package com.uservoice.uservoicesdk.activity;

import com.uservoice.uservoicesdk.ui.ContactAdapter;
import com.uservoice.uservoicesdk.ui.InstantAnswersAdapter;

public class ContactActivity extends InstantAnswersActivity {

	@Override
	protected InstantAnswersAdapter createAdapter() {
		return new ContactAdapter(this);
	}
}