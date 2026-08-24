package com.example.moodchon.domain.chonkangs.dto;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsStatus;

public enum ChonkangsFilter {
    ALL {
        @Override
        public boolean matches(ChonkangsStatus status) {
            return true;
        }
    },
    ONGOING {
        @Override
        public boolean matches(ChonkangsStatus status) {
            return status == ChonkangsStatus.ONGOING;
        }
    },
    COMPLETED {
        @Override
        public boolean matches(ChonkangsStatus status) {
            return status == ChonkangsStatus.COMPLETED;
        }
    };

    public abstract boolean matches(ChonkangsStatus status);
}
