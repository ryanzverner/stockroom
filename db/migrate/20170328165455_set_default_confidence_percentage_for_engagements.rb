Sequel.migration do
  up do
    alter_table :engagements do
      set_column_not_null :confidence_percentage
    end
  end

  down do
    alter_table :engagements do
      set_column_allow_null :confidence_percentage
    end
  end
end